package service.AAADEVMIXER;

import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;
import com.avaya.workflow.logger.*;

import org.apache.commons.lang3.StringUtils;

import service.util.Delete;
import service.util.MixingAudioInputStream;

public class MixerExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory
			.getLogger(MixerExecution.class);

	public MixerExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		logger.info("MixerExecution");
		String FQDN = null;
		try {
			int i = 0;
			
			String firstAudioURL = (String) get("urlFirstAudio");
			String audioUno;
			String audioDos;
			float tiempoFinal = 0;
			File EndFile = null;

			if (StringUtils.isBlank(firstAudioURL)) {
				throw new IllegalArgumentException(
						"Invalid Argument! urlFirstAudio cannot be empty...");
			} else {
				String[] separate = firstAudioURL.split("/");
				FQDN = separate[2];
				audioUno = separate[separate.length - 1];
			}
			String secondAudioURL = (String) get("urlSecondAudio");
			if (StringUtils.isBlank(secondAudioURL)) {
				throw new IllegalArgumentException(
						"Invalid Argument! urlFirstAudio cannot be empty...");
			} else {
				String[] separate = secondAudioURL.split("/");
				FQDN = separate[2];
				audioDos = separate[separate.length - 1];
			}

			File audio1 = new File("/home/wsuser/web/RecordParticipant/"
					+ audioUno);

			File audio2 = new File("/home/wsuser/web/RecordParticipant/"
					+ audioDos);

			File audioVoid = new File("/home/wsuser/web/Grabaciones/void.wav");
			/*
			 * Audio InputStream Audio 1
			 */
			float durationInSeconds = tiempoAudio(audio1);
			/*
			 * Audio InputStream Audio 2
			 */
			float durationInSeconds2 = tiempoAudio(audio2);

			float diferenciaSegundos = 0;
			/*
			 * Determinamos sí Audio2 es Mayor
			 */
			if (durationInSeconds2 >= durationInSeconds) {
				logger.info("MixerExecution Audio2 es mayor: "
						+ durationInSeconds2);

				// Tiempo Mayor audio2
				// Obtener la diferencia de tiempos
				diferenciaSegundos = (durationInSeconds2 - durationInSeconds);
				tiempoFinal = durationInSeconds2;
				/*
				 * Si la diferencia de tiempo es solo de 1 segundo, solo se
				 * Mezclan los audios
				 */
				if (diferenciaSegundos <= 1) {
					logger.info("MixerExecution Diferencia de seg menor a uno "
							+ diferenciaSegundos);
					AudioInputStream clip1 = AudioSystem
							.getAudioInputStream(audio1);
					AudioInputStream clip2 = AudioSystem
							.getAudioInputStream(audio2);
					Collection list = new ArrayList();
					list.add(clip1);
					list.add(clip2);
					DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					Date date = new Date(System.currentTimeMillis());
					String[] handleOne = audio1.getName().split("_");
					String[] handleTwo = audio2.getName().split("_");
					File mixFile = new File(
							"/home/wsuser/web/Grabaciones/wavMixed.wav");

					AudioInputStream mixedFiles = new MixingAudioInputStream(
							clip2.getFormat(), list);

					AudioSystem.write(mixedFiles, AudioFileFormat.Type.WAVE,
							mixFile);
					copyAudio(
							mixFile.getAbsolutePath(),
							"/home/wsuser/web/Conversaciones/"
									+ df.format(date) + "_" + handleOne[0]
									+ "_" + handleTwo[0] + "_" + handleTwo[1],
							0, Math.round(tiempoFinal));

					JSONObject jsonResult = new JSONObject();
					jsonResult.put("status", "SUCCESS");
					jsonResult
							.put("urlOut",
									"https://"+FQDN+"/services/AAADEVLOGGER/AAACallRecording.html");
					return jsonResult;
				}
				int diferenciaEnSegundos = (Math.round(diferenciaSegundos));
				logger.info("MixerExecution Diferencia de seg "
						+ diferenciaEnSegundos);
				// Definimos el tiempo máximo de la grabación.
				double cortes = (tiempoFinal / diferenciaSegundos);
				AudioInputStream appendedFiles = null;
				int primerParametro = 0;
				double SegundoParametro = cortes;
				int ciclo = diferenciaEnSegundos;
				File audioSec = new File(
						"/home/wsuser/web/audioSecuencias/audioC1.wav");
				for (i = 1; i <= ciclo; i++) {

					copyAudioFloat(audio1.getAbsolutePath(),
							audioSec.getAbsolutePath(), primerParametro,
							SegundoParametro);
					File audio3 = new File(audioSec.getAbsolutePath());
					AudioInputStream clip1 = AudioSystem
							.getAudioInputStream(audio3);
					AudioInputStream clipVoid = AudioSystem
							.getAudioInputStream(audioVoid);

					appendedFiles = new AudioInputStream(
							new SequenceInputStream(clip1, clipVoid),
							clip1.getFormat(), clip1.getFrameLength()
									+ clipVoid.getFrameLength());

					AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE,
							new File("home/wsuser/web/audioSecuencias/audioC1"
									+ getCharForNumber(i) + ".wav"));

					primerParametro += (cortes);
					appendedFiles.close();

				}
				try {
					File audDir = new File("home/wsuser/web/audioSecuencias/");

					File[] filesList = audDir.listFiles();
					long length = 0;
					AudioInputStream clip = null;
					List<AudioInputStream> list = new ArrayList<AudioInputStream>();

					for (File file : filesList) {
						clip = AudioSystem.getAudioInputStream(new File(file
								.getPath()));
						list.add(clip);
						length += clip.getFrameLength();

					}

					AudioInputStream appendedFiles2 = null;
					if (length > 0 && list.size() > 0 && clip != null) {

						appendedFiles2 = new AudioInputStream(
								new SequenceInputStream(
										Collections.enumeration(list)),
								clip.getFormat(), length);
						File file = new File(
								"/home/wsuser/web/Grabaciones/wavSequenced.wav");
						AudioSystem.write(appendedFiles2,
								AudioFileFormat.Type.WAVE, file);
					}
				} catch (Exception e) {
					logger.error("MixerExecution " + e.toString());
				}
				AudioInputStream clip1 = AudioSystem
						.getAudioInputStream(new File(
								"/home/wsuser/web/Grabaciones/wavSequenced.wav"));
				AudioInputStream clip2 = AudioSystem
						.getAudioInputStream(audio2);

				Collection list = new ArrayList();
				list.add(clip1);
				list.add(clip2);
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date(System.currentTimeMillis());
				String[] handleOne = audio1.getName().split("_");
				String[] handleTwo = audio2.getName().split("_");
				File mixFile = new File(
						"/home/wsuser/web/Grabaciones/wavMixed.wav");
				EndFile = new File("home/wsuser/web/Conversaciones/"
						+ df.format(date) + "_" + handleOne[0] + "_"
						+ handleTwo[0] + "_" + handleTwo[1]);
				AudioInputStream mixedFiles = new MixingAudioInputStream(
						clip2.getFormat(), list);

				AudioSystem.write(mixedFiles, AudioFileFormat.Type.WAVE,
						mixFile);
				copyAudio(mixFile.getAbsolutePath(), EndFile.getAbsolutePath(),
						0, Math.round(tiempoFinal));

			} else {
				// Tiempo Mayor audio1
				logger.info("MixerExecution Audio1 es mayor: "
						+ durationInSeconds);
				// Obtener la diferencia de tiempos
				diferenciaSegundos = (durationInSeconds - durationInSeconds2);
				tiempoFinal = durationInSeconds;
				if (diferenciaSegundos <= 1) {
					logger.info("MixerExecution Diferencia de seg menor a uno "
							+ diferenciaSegundos);
					AudioInputStream clip1 = AudioSystem
							.getAudioInputStream(audio1);
					AudioInputStream clip2 = AudioSystem
							.getAudioInputStream(audio2);
					Collection list = new ArrayList();
					list.add(clip1);
					list.add(clip2);
					DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					Date date = new Date(System.currentTimeMillis());
					String[] handleOne = audio1.getName().split("_");
					String[] handleTwo = audio2.getName().split("_");
					File mixFile = new File(
							"/home/wsuser/web/Grabaciones/wavMixed.wav");

					AudioInputStream mixedFiles = new MixingAudioInputStream(
							clip2.getFormat(), list);

					AudioSystem.write(mixedFiles, AudioFileFormat.Type.WAVE,
							mixFile);
					copyAudio(mixFile.getAbsolutePath(),
							"home/wsuser/web/Conversaciones/" + df.format(date)
									+ "_" + handleOne[0] + "_" + handleTwo[0]
									+ "_" + handleTwo[1], 0,
							Math.round(tiempoFinal));

					JSONObject jsonResult = new JSONObject();
					jsonResult.put("status", "SUCCESS");
					jsonResult
							.put("urlOut",
									"https://"+FQDN+"/services/AAADEVLOGGER/AAACallRecording.html");
					return jsonResult;
				}
				int diferenciaEnSegundos = (Math.round(diferenciaSegundos));
				logger.info("MixerExecution Diferencia de seg "
						+ diferenciaEnSegundos);
				// Definimos el tiempo máximo de la grabación.
				double cortes = (tiempoFinal / diferenciaSegundos);
				AudioInputStream appendedFiles = null;
				int primerParametro = 0;
				double SegundoParametro = cortes;
				int ciclo = diferenciaEnSegundos;
				File audioSec = new File(
						"/home/wsuser/web/audioSecuencias/audioC1.wav");
				for (i = 1; i <= ciclo; i++) {

					copyAudioFloat(audio2.getAbsolutePath(),
							audioSec.getAbsolutePath(), primerParametro,
							SegundoParametro);
					File audio3 = new File(audioSec.getAbsolutePath());
					AudioInputStream clip1 = AudioSystem
							.getAudioInputStream(audio3);
					AudioInputStream clipVoid = AudioSystem
							.getAudioInputStream(audioVoid);

					appendedFiles = new AudioInputStream(
							new SequenceInputStream(clip1, clipVoid),
							clip1.getFormat(), clip1.getFrameLength()
									+ clipVoid.getFrameLength());

					AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE,
							new File("home/wsuser/web/audioSecuencias/audioC1"
									+ getCharForNumber(i) + ".wav"));

					primerParametro += (cortes);
					appendedFiles.close();

				}
				try {
					File audDir = new File("home/wsuser/web/audioSecuencias/");

					File[] filesList = audDir.listFiles();
					long length = 0;
					AudioInputStream clip = null;
					List<AudioInputStream> list = new ArrayList<AudioInputStream>();

					for (File file : filesList) {
						clip = AudioSystem.getAudioInputStream(new File(file
								.getPath()));
						list.add(clip);
						length += clip.getFrameLength();

					}

					AudioInputStream appendedFiles2 = null;
					if (length > 0 && list.size() > 0 && clip != null) {

						appendedFiles2 = new AudioInputStream(
								new SequenceInputStream(
										Collections.enumeration(list)),
								clip.getFormat(), length);
						File file = new File(
								"/home/wsuser/web/Grabaciones/wavSequenced.wav");
						AudioSystem.write(appendedFiles2,
								AudioFileFormat.Type.WAVE, file);
					}
				} catch (Exception e) {
					logger.error("MixerExecution " + e.toString());
				}
				AudioInputStream clip1 = AudioSystem
						.getAudioInputStream(new File(
								"/home/wsuser/web/Grabaciones/wavSequenced.wav"));
				AudioInputStream clip2 = AudioSystem
						.getAudioInputStream(audio1);

				Collection list = new ArrayList();
				list.add(clip1);
				list.add(clip2);
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date(System.currentTimeMillis());
				String[] handleOne = audio1.getName().split("_");
				String[] handleTwo = audio2.getName().split("_");
				File mixFile = new File(
						"/home/wsuser/web/Grabaciones/wavMixed.wav");
				EndFile = new File("home/wsuser/web/Conversaciones/"
						+ df.format(date) + "_" + handleOne[0] + "_"
						+ handleTwo[0] + "_" + handleTwo[1]);
				AudioInputStream mixedFiles = new MixingAudioInputStream(
						clip2.getFormat(), list);

				AudioSystem.write(mixedFiles, AudioFileFormat.Type.WAVE,
						mixFile);
				copyAudio(mixFile.getAbsolutePath(), EndFile.getAbsolutePath(),
						0, Math.round(tiempoFinal));
			}

			for (int j = 1; j < i; j++) {

				String Uri = "http://"
						+ FQDN
						+ "/services/AAADEVLOGGER/ReadText/web/audioSecuencias/audioC1"
						+ getCharForNumber(j) + ".wav";
				Delete deleteSec = new Delete();
				try {
					logger.info("MixerExecution "
							+ deleteSec.borrarSecuencia(Uri));
				} catch (Exception ex) {
					logger.error("MixerExecution " + ex.toString());
				}

			}

		} catch (Exception e) {
			throw new Exception(e.toString());
		}

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("status", "SUCCESS");
		jsonResult
				.put("urlOut",
						"https://"+FQDN+"/services/AAADEVLOGGER/AAACallRecording.html");
		return jsonResult;
	}

	public static float tiempoAudio(File audio)
			throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioInputStream = AudioSystem
				.getAudioInputStream(audio);
		AudioFormat format = audioInputStream.getFormat();
		long audioFileLength = audio.length();
		int frameSize = format.getFrameSize();
		float frameRate = format.getFrameRate();
		float durationInSeconds = (audioFileLength / (frameSize * frameRate));
		return durationInSeconds;
	}

	public static void copyAudio(String sourceFileName,
			String destinationFileName, int startSecond, int secondsToCopy) {
		AudioInputStream inputStream = null;
		AudioInputStream shortenedStream = null;
		try {
			File file = new File(sourceFileName);
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			AudioFormat format = fileFormat.getFormat();
			inputStream = AudioSystem.getAudioInputStream(file);
			int bytesPerSecond = format.getFrameSize()
					* (int) format.getFrameRate();
			inputStream.skip(startSecond * bytesPerSecond);
			long framesOfAudioToCopy = secondsToCopy
					* (int) format.getFrameRate();
			shortenedStream = new AudioInputStream(inputStream, format,
					framesOfAudioToCopy);
			File destinationFile = new File(destinationFileName);
			AudioSystem.write(shortenedStream, fileFormat.getType(),
					destinationFile);
		} catch (Exception e) {
			// println(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					// println(e);
				}
			}
			if (shortenedStream != null) {
				try {
					shortenedStream.close();
				} catch (Exception e) {
					// println(e);
				}
			}
		}
	}

	public static void copyAudioFloat(String sourceFileName,
			String destinationFileName, double startSecond, double secondsToCopy) {
		AudioInputStream inputStream = null;
		AudioInputStream shortenedStream = null;
		try {
			File file = new File(sourceFileName);
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
			AudioFormat format = fileFormat.getFormat();
			inputStream = AudioSystem.getAudioInputStream(file);
			int bytesPerSecond = format.getFrameSize()
					* (int) format.getFrameRate();
			inputStream.skip((long) (startSecond * bytesPerSecond));
			long framesOfAudioToCopy = (long) (secondsToCopy * (int) format
					.getFrameRate());
			shortenedStream = new AudioInputStream(inputStream, format,
					framesOfAudioToCopy);
			File destinationFile = new File(destinationFileName);
			AudioSystem.write(shortenedStream, fileFormat.getType(),
					destinationFile);

		} catch (Exception e) {

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					// println(e);
				}
			}
			if (shortenedStream != null) {
				try {
					shortenedStream.close();
				} catch (Exception e) {
					// println(e);
				}
			}
		}
	}

	private static String getCharForNumber(int i) {
		return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;
	}
}
