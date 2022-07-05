package com.ikno.itracclient.sounds;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineEvent.Type;

import com.ikno.dao.business.Incident.Level;
import com.ikno.dao.utils.Configuration;

public class WAVPlayer {

	private static Map<String,String> waveFiles = new HashMap<String,String>();
	
	static {
		waveFiles.put("chimes", "chimes.wav");
		waveFiles.put("chord", "chord.wav");
		waveFiles.put("error", "error.wav");
		waveFiles.put("fine", "ding.wav");
		waveFiles.put("info", "notify.wav");
		waveFiles.put("recycle", "recycle.wav");
		waveFiles.put("ringin", "ringin.wav");
		waveFiles.put("ringout", "ringout.wav");
		waveFiles.put("severe", "error.wav");
		waveFiles.put("tada", "tada.wav");
		waveFiles.put("warning", "warning.wav");
	}
	
	public static void playWave(String waveName) {
		try {
			if (Configuration.configCenter().getBoolean("com.ikno.playsounds",true) == false)
				return;
			String fileName = waveFiles.get(waveName);
			InputStream istream = null;
			if (fileName != null) {
				File file = new File(fileName);
				if (file.exists())
					istream = new FileInputStream(file);
				else
					istream = WAVPlayer.class.getResourceAsStream(fileName);
			}
			if (istream == null) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			AudioInputStream stream = AudioSystem.getAudioInputStream(istream);
	        // At present, ALAW and ULAW encodings must be converted
	        // to PCM_SIGNED before it can be played
	        AudioFormat format = stream.getFormat();
	        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
	            format = new AudioFormat(
	                    AudioFormat.Encoding.PCM_SIGNED,
	                    format.getSampleRate(),
	                    format.getSampleSizeInBits()*2,
	                    format.getChannels(),
	                    format.getFrameSize()*2,
	                    format.getFrameRate(),
	                    true);        // big endian
	            stream = AudioSystem.getAudioInputStream(format, stream);
	        }
	    
	        // Create the clip
	        DataLine.Info info = new DataLine.Info(
	            Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
	        Clip clip = (Clip) AudioSystem.getLine(info);
	    
	        // This method does not return until the audio file is completely loaded
	        clip.open(stream);
	    
	        // Start playing
	        clip.start();
	        clip.addLineListener(new LineListener() {
				public void update(LineEvent event) {
					if (event.getType() == Type.STOP) {
						Clip clip = (Clip)event.getLine();
						clip.close();
					}
				}
	        });
	    } catch (MalformedURLException e) {
	    } catch (IOException e) {
	    } catch (LineUnavailableException e) {
	    } catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
