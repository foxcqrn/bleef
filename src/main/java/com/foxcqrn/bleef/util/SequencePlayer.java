package com.foxcqrn.bleef.util;

import com.foxcqrn.bleef.protos.SequenceProto;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

class SoundType {
    float pitch;  // set
    float volume;  // mult
    Sound sound;
    SoundType(float volume, float pitch, Sound sound) {
        this.pitch = pitch;
        this.volume = volume;
        this.sound = sound;
    }
}

class NormalizedSoundParams {
    Sound inst;
    float volume;
    float pitch;
    public NormalizedSoundParams(Sound inst, float volume, float pitch) {
        this.inst = inst;
        this.volume = volume;
        this.pitch = pitch;
    }
}

enum DrumSound {
    KICK,
    SNARE,
    HAT,
    OPEN_HAT,
    STICKS,
    CRASH,
    RIDE,
    SHAKER;

    SoundType getSound() {
        switch (this) {
            case KICK:
                return new SoundType(1.0f, 4.0f, Sound.BLOCK_NOTE_BLOCK_BASEDRUM);
            case SNARE:
                return new SoundType(1.0f, 7.0f, Sound.BLOCK_NOTE_BLOCK_SNARE);
            case HAT:
            case OPEN_HAT:  // idk
                return new SoundType(1.0f, 12.0f, Sound.BLOCK_NOTE_BLOCK_HAT);
            case STICKS:
                return new SoundType(1.0f, 3.0f, Sound.BLOCK_NOTE_BLOCK_HAT);
            case CRASH:
            case RIDE:
                return new SoundType(1.0f, 6.0f, Sound.BLOCK_FIRE_EXTINGUISH);
            case SHAKER:
                return new SoundType(1.0f, 10.0f, Sound.BLOCK_SUSPICIOUS_SAND_PLACE);
            default:
                return null;
        }
    }
}

public class SequencePlayer {
    boolean playing;

    public Thread getThread(Object executor, int sequenceId, Callable<Boolean> onNote, Callable<Boolean> onEnded) {
        Player player;
        Location location;
        if (executor instanceof Player) {
            location = null;
            player = (Player) executor;
        } else {
            player = null;
            if (executor instanceof Location) {
                location = (Location) executor;
            } else {
                throw new RuntimeException("'executor' must be either Player or Location");
            }
        }
        Runnable sequenceProcessor = () -> {
            String urlStr = String.format("https://onlinesequencer.net/app/api/get_proto.php?id=%d", sequenceId);
            URL url;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                if (player != null) player.sendMessage(ChatColor.RED + "Could not format URL");
                return;
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                if (player != null) player.sendMessage(ChatColor.RED + "Could not connect to server");
            }
            try {
                assert conn != null;
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                // shouldn't happen
                return;
            }
            try {
                int respCode = conn.getResponseCode();
                if (respCode != 200) {
                    if (player != null) player.sendMessage(ChatColor.RED + "Invalid sequence! (" + respCode + ")");
                    return;
                }
            } catch (IOException e) {
                if (player != null) player.sendMessage(ChatColor.RED + "Could not retrieve status");
            }
            try (InputStream stream = conn.getInputStream()) {
                SequenceProto.Sequence seq = SequenceProto.Sequence.parseFrom(stream);
                List<SequenceProto.Note> notes = new ArrayList<>(seq.getNotesList());
                notes.sort((SequenceProto.Note a, SequenceProto.Note b) -> (int)((a.getTime() - b.getTime()) * 1000));
                AtomicReference<Float> lastTime = new AtomicReference<>((float) 0);
                float bpm = (float) seq.getSettings().getBpm();
//                float sleepTime = 1f / ((bpm  * 4f) / 60f) * 1000f;
                float sleepTime = 15000f / bpm;
                if (player != null) player.sendMessage(ChatColor.AQUA + "Playing sequence " + sequenceId);
                this.playing = true;
                notes.forEach((SequenceProto.Note note) -> {
                    if (!this.playing) return;
                    float time = note.getTime();
                    if (time > lastTime.get()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep((long) (sleepTime * (time - lastTime.get())));
                        } catch (InterruptedException e) {
                            if (player != null) player.sendMessage(ChatColor.RED + "Can't sleep!");
                        }
                    }
                    if (player != null) {
                        playNote(player, note, seq.getSettings().getInstrumentsMap().get(note.getInstrument()));
                    } else {
                        playNote(location, note, seq.getSettings().getInstrumentsMap().get(note.getInstrument()));
                    }
                    lastTime.set(time);
                    try {
                        onNote.call();
                    } catch (Exception e) {
                        Logger.getLogger("Bleef").log(Level.INFO, "Failed to execute callback onNote for SequencePlayer");
                    }
                });
                onEnded.call();
            } catch (IOException e) {
                if (player != null) player.sendMessage(ChatColor.RED + "Could not read");
            } catch (Exception e) {
                Logger.getLogger("Bleef").log(Level.INFO, "Failed to execute callback onEnded for SequencePlayer");
            }
        };
        return new Thread(sequenceProcessor);
    }

    public void stop() {
        this.playing = false;
    }

    private @Nullable DrumSound translateDrumKitOrElectricDrumKit(int note) {
        switch (note) {
            case 31:
            case 33:
            case 37:
                return DrumSound.STICKS;
            case 35:
            case 36:
                return DrumSound.KICK;
            case 38:
            case 39:
            case 40:
                return DrumSound.SNARE;
            case 42:
            case 44:
                return DrumSound.HAT;
            case 46:
                return DrumSound.OPEN_HAT;
            case 49:
            case 57:
                return DrumSound.CRASH;
            case 51:
            case 59:
                return DrumSound.RIDE;
            case 82:
                return DrumSound.SHAKER;
            default:
                return null;
        }
    }

    private @Nullable DrumSound translate808DrumKit(int note) {
        switch (note) {
            case 27:
            case 28:
            case 29:
                return DrumSound.KICK;
            case 30:
            case 31:
            case 32:
            case 33:
                return DrumSound.SNARE;
            case 34:
            case 44:
                return DrumSound.STICKS;
            case 35:
                return DrumSound.HAT;
            case 36:
                return DrumSound.OPEN_HAT;
            case 37:
                return DrumSound.CRASH;
            default:
                return null;
        }
    }

    private @Nullable DrumSound translate8BitDrumKit(int note) {
        switch (note) {
            case 31:
                return DrumSound.KICK;
            case 32:
            case 34:
            case 35:
            case 36:
                return DrumSound.SNARE;
            case 33:
                return DrumSound.HAT;
            default:
                return null;
        }
    }

    private @Nullable DrumSound translate2013DrumKit(int note) {
        switch (note) {
            case 31:
            case 32:
                return DrumSound.KICK;
            case 33:
            case 34:
            case 35:
            case 36:
            case 56:
            case 58:
                return DrumSound.SNARE;
            case 38:
                return DrumSound.HAT;
            case 40:
                return DrumSound.OPEN_HAT;
            case 42:
                return DrumSound.CRASH;  // close
            case 45:
            case 48:
            case 53:
            case 57:
                return DrumSound.CRASH;
            case 47:
            case 54:
                return DrumSound.RIDE;
            default:
                return null;
        }
    }

    private @Nullable DrumSound translate909DrumKit(int note) {
        switch (note) {
            case 27:
            case 28:
            case 29:
                return DrumSound.KICK;
            case 30:
            case 31:
            case 32:
            case 40:
                return DrumSound.SNARE;
            case 41:
            case 42:
                return DrumSound.HAT;
            case 43:
            case 44:
                return DrumSound.OPEN_HAT;
            case 45:
                return DrumSound.CRASH;
            case 46:
                return DrumSound.RIDE;
            case 39:
                return DrumSound.STICKS;
            default:
                return null;
        }
    }

    private @Nullable DrumSound translate2023DrumKit(int note) {
        switch (note) {
            case 24:
            case 30:
                return DrumSound.STICKS;
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return DrumSound.KICK;
            case 31:
            case 32:
            case 33:
                return DrumSound.SNARE;
            case 42:
            case 44:
                return DrumSound.HAT;
            case 43:
                return DrumSound.OPEN_HAT;
            case 46:
            case 47:
            case 48:
                return DrumSound.CRASH;
            case 49:
            case 50:
                return DrumSound.RIDE;
            default:
                return null;
        }
    }

    private @Nullable Sound translateInstrument(int instrument) {
        int baseInst = instrument % 10000;
        switch (baseInst) {
            case 1:
            case 4:
            case 22:
            case 32:
            case 33:
            case 35:
            case 38:
            case 44:
            case 49:
                return Sound.BLOCK_NOTE_BLOCK_GUITAR;
            case 3:
            case 20:
            case 28:
            case 45:
            case 46:
            case 47:
                return Sound.BLOCK_NOTE_BLOCK_CHIME;
            case 5:
            case 29:
            case 37:
            case 48:
            case 54:
                return Sound.BLOCK_NOTE_BLOCK_BASS;
            case 6:
            case 7:
            case 11:
            case 12:
            case 52:
                return Sound.BLOCK_NOTE_BLOCK_PLING;
            case 9:
            case 10:
            case 23:
            case 24:
            case 50:
            case 51:
                return Sound.BLOCK_NOTE_BLOCK_FLUTE;
            case 13:
            case 14:
            case 15:
            case 16:
            case 27:
            case 30:
            case 55:
            case 56:
            case 57:
                return Sound.BLOCK_NOTE_BLOCK_BIT;
            case 19:
                return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE;
            case 21:
            case 34:
                return Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE;
            case 2:
            case 31:
            case 39:
            case 36:
            case 40:
            case 42:
            case 53:
                return null;
            default:
                return Sound.BLOCK_NOTE_BLOCK_HARP;
        }
    }

    private NormalizedSoundParams getParams(SequenceProto.Note note, SequenceProto.InstrumentSettings instSettings) {
        if (note.getVolume() == 0) {
            return null;
        }
        Sound inst = translateInstrument(note.getInstrument());
        float pitch = (float)note.getTypeValue() + 6f;
        if (instSettings != null) {
            pitch += instSettings.getDetune() / 100;
        }
        if (pitch < 0) {
            pitch = 0f;
        }
        if (pitch > 24) {
            pitch = 12 + (pitch % 12);
        }
        float volume = note.getVolume();
        if (inst == null) {
            DrumSound drumInst = null;
            switch (note.getInstrument()) {
                case 2:
                case 31:
                    drumInst = translateDrumKitOrElectricDrumKit(note.getTypeValue());
                    break;
                case 39:
                    drumInst = translate8BitDrumKit(note.getTypeValue());
                    break;
                case 36:
                    drumInst = translate808DrumKit(note.getTypeValue());
                    break;
                case 40:
                    drumInst = translate2013DrumKit(note.getTypeValue());
                    break;
                case 42:
                    drumInst = translate909DrumKit(note.getTypeValue());
                    break;
                case 53:
                    drumInst = translate2023DrumKit(note.getTypeValue());
                    break;
            }
            if (drumInst == null || drumInst.getSound() == null) {
                return null;
            }
            SoundType sound = drumInst.getSound();
            inst = sound.sound;
            volume *= sound.volume;
            pitch = sound.pitch;
        }
        return new NormalizedSoundParams(inst, volume, (float) Math.pow(2.0, ((double) pitch - 12.0) / 12.0));
    }

    private void playNote(Player player, SequenceProto.Note note, SequenceProto.InstrumentSettings instSettings) {
        NormalizedSoundParams params = this.getParams(note, instSettings);
        if (params == null) return;
        player.playSound(player.getLocation(), params.inst, params.volume, params.pitch);
    }

    private void playNote(Location location, SequenceProto.Note note, SequenceProto.InstrumentSettings instSettings) {
        NormalizedSoundParams params = this.getParams(note, instSettings);
        if (params == null) return;
        Objects.requireNonNull(location.getWorld()).playSound(location, params.inst, params.volume * 2, params.pitch);
    }
}