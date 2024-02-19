package com.foxcqrn.bleef.commands;

import com.foxcqrn.bleef.Bleef;
import com.foxcqrn.bleef.protos.SequenceProto;
import org.bukkit.*;
import org.bukkit.Note;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.foxcqrn.bleef.protos.SequenceProto.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CommandSequence implements CommandExecutor {
    HashSet<UUID> sequencePlayers = new HashSet<>();

    public CommandSequence(Bleef plugin) {
        Bleef.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0 || args[0].isEmpty()) {
            sequencePlayers.remove(player.getUniqueId());
            sender.sendMessage(ChatColor.AQUA + "Stopping sequence.");
            return true;
        }
        int sequenceId;
        try {
            sequenceId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Sequence ID must be an integer.");
            return true;
        }

        sequencePlayers.add(player.getUniqueId());

        Thread thread = getThread(player, sequenceId);
        thread.start();

        return true;
    }

    private Thread getThread(Player player, int sequenceId) {
        Runnable sequenceProcessor = () -> {
            String urlStr = String.format("https://onlinesequencer.net/app/api/get_proto.php?id=%d", sequenceId);
            URL url;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                player.sendMessage(ChatColor.RED + "Could not format URL");
                return;
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Could not connect to server");
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
                    player.sendMessage(ChatColor.RED + "Invalid sequence! (" + respCode + ")");
                    return;
                }
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Could not retrieve status");
            }
            try (InputStream stream = conn.getInputStream()) {
                Sequence seq = Sequence.parseFrom(stream);
                List<SequenceProto.Note> notes = new ArrayList<>(seq.getNotesList());
                notes.sort((SequenceProto.Note a, SequenceProto.Note b) -> (int)((a.getTime() - b.getTime()) * 1000));
                AtomicReference<Float> lastTime = new AtomicReference<>((float) 0);
                float bpm = (float) seq.getSettings().getBpm();
                float sleepTime = 1f / ((bpm  * 4f) / 60f) * 1000f;
                player.sendMessage(ChatColor.AQUA + "Playing sequence " + sequenceId);
                notes.forEach((SequenceProto.Note note) -> {
                    if (!sequencePlayers.contains(player.getUniqueId())) {
                        return;
                    }
                    float time = note.getTime();
                    if (time > lastTime.get()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep((long) (sleepTime * (time - lastTime.get())));
                        } catch (InterruptedException e) {
                            player.sendMessage(ChatColor.RED + "Can't sleep!");
                        }
                    }
                    playNote(player, note, seq.getSettings().getInstrumentsMap().get(note.getInstrument()));
                    lastTime.set(time);
                });
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Could not read");
            }
        };
        return new Thread(sequenceProcessor);
    }

    private @Nullable Instrument translateDrumKitOrElectricDrumKit(int note) {
        switch (note) {
            case 35:
            case 36:
                return Instrument.BASS_DRUM;
            case 38:
            case 39:
            case 40:
                return Instrument.SNARE_DRUM;
            case 42:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translate808DrumKit(int note) {
        switch (note) {
            case 27:
            case 28:
            case 29:
                return Instrument.BASS_DRUM;
            case 30:
            case 31:
            case 32:
            case 33:
                return Instrument.SNARE_DRUM;
            case 35:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translate8BitDrumKit(int note) {
        switch (note) {
            case 31:
                return Instrument.BASS_DRUM;
            case 32:
            case 34:
            case 35:
            case 36:
                return Instrument.SNARE_DRUM;
            case 33:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translate2013DrumKit(int note) {
        switch (note) {
            case 31:
            case 32:
                return Instrument.BASS_DRUM;
            case 33:
            case 34:
            case 35:
            case 36:
            case 56:
            case 58:
                return Instrument.SNARE_DRUM;
            case 38:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translate909DrumKit(int note) {
        switch (note) {
            case 27:
            case 28:
            case 29:
                return Instrument.BASS_DRUM;
            case 30:
            case 31:
            case 32:
                return Instrument.SNARE_DRUM;
            case 41:
            case 42:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translate2023DrumKit(int note) {
        switch (note) {
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
                return Instrument.BASS_DRUM;
            case 30:
            case 31:
            case 32:
            case 33:
                return Instrument.SNARE_DRUM;
            case 42:
            case 44:
                return Instrument.STICKS;
            default:
                return null;
        }
    }

    private @Nullable Instrument translateInstrument(int instrument) {
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
                return Instrument.GUITAR;
            case 3:
            case 20:
            case 28:
            case 45:
            case 46:
            case 47:
                return Instrument.CHIME;
            case 5:
            case 29:
            case 37:
            case 48:
            case 54:
                return Instrument.BASS_GUITAR;
            case 6:
            case 7:
            case 11:
            case 12:
            case 52:
                return Instrument.PLING;
            case 9:
            case 10:
            case 23:
            case 24:
            case 50:
            case 51:
                return Instrument.FLUTE;
            case 13:
            case 14:
            case 15:
            case 16:
            case 27:
            case 30:
            case 55:
            case 56:
            case 57:
                return Instrument.BIT;
            case 19:
                return Instrument.XYLOPHONE;
            case 21:
            case 34:
                return Instrument.IRON_XYLOPHONE;
            case 2:
            case 31:
            case 39:
            case 36:
            case 40:
            case 42:
            case 53:
                return null;
            default:
                return Instrument.PIANO;
        }
    }

    private void playNote(Player player, SequenceProto.Note note, SequenceProto.InstrumentSettings instSettings) {
        if (note.getVolume() == 0) {
            return;
        }
        Instrument inst = translateInstrument(note.getInstrument());
        int pitch = note.getTypeValue() + 6;
        if (instSettings != null) {
            pitch += Math.round(instSettings.getDetune());
        }
        if (pitch < 0) {
            pitch = 0;
        }
        if (pitch > 24) {
            pitch = 12 + (pitch % 12);
        }
        if (inst == null) {
            switch (note.getInstrument()) {
                case 2:
                case 31:
                    inst = translateDrumKitOrElectricDrumKit(note.getTypeValue());
                    break;
                case 39:
                    inst = translate8BitDrumKit(note.getTypeValue());
                    break;
                case 36:
                    inst = translate808DrumKit(note.getTypeValue());
                    break;
                case 40:
                    inst = translate2013DrumKit(note.getTypeValue());
                    break;
                case 42:
                    inst = translate909DrumKit(note.getTypeValue());
                    break;
                case 53:
                    inst = translate2023DrumKit(note.getTypeValue());
                    break;
            }
            if (inst == null) {
                return;
            }
            switch (inst) {
                case BASS_DRUM:
                    pitch = 4;
                    break;
                case SNARE_DRUM:
                    pitch = 7;
                    break;
                case STICKS:
                    pitch = 12;
                    break;
                default:
                    pitch = 0;
            }
        }

        player.playNote(player.getLocation(), inst, new Note(pitch));
    }
}
