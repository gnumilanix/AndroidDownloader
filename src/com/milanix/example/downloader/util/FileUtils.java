package com.milanix.example.downloader.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import com.milanix.example.downloader.R;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * This class contains file utils
 * 
 * @author Milan
 * 
 */
public class FileUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	private static final HashMap<String, FileType> textMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> codeMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> audioMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> imageMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> videoMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> archiveMap = new HashMap<String, FileType>();
	private static final HashMap<String, FileType> pdfMap = new HashMap<String, FileType>();

	// Size constants
	private final static long SIZE_KB = 1024L;
	private final static long SIZE_MB = SIZE_KB * SIZE_KB;
	private final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	// Treshold in percentage
	public static final long STORAGE_THRESHOLD = 10;

	/**
	 * Enum to define supported file types
	 * 
	 * @author Milan
	 * 
	 */
	public static enum FileType {
		FILE, DOCUMENT, CODE, AUDIO, IMAGE, VIDEO, ARCHIVE, PDF
	}

	/**
	 * Enum to define storage types
	 * 
	 * @author Milan
	 * 
	 */
	public static enum StorageSize {
		KB, MB, GB
	}

	/*
	 * Add all supported document text types Based on okhttp
	 * libcore.net.MimeUtils
	 */
	static {
		textMap.put("ics", FileType.DOCUMENT);
		textMap.put("icz", FileType.DOCUMENT);
		textMap.put("csv", FileType.DOCUMENT);
		textMap.put("323", FileType.DOCUMENT);
		textMap.put("uls", FileType.DOCUMENT);
		textMap.put("mml", FileType.DOCUMENT);
		textMap.put("txt", FileType.DOCUMENT);
		textMap.put("asc", FileType.DOCUMENT);
		textMap.put("text", FileType.DOCUMENT);
		textMap.put("diff", FileType.DOCUMENT);
		textMap.put("po", FileType.DOCUMENT);
		textMap.put("rtx", FileType.DOCUMENT);
		textMap.put("rtf", FileType.DOCUMENT);
		textMap.put("etx", FileType.DOCUMENT);
		textMap.put("tcl", FileType.DOCUMENT);
		textMap.put("tex", FileType.DOCUMENT);
		textMap.put("ltx", FileType.DOCUMENT);
		textMap.put("sty", FileType.DOCUMENT);
		textMap.put("cls", FileType.DOCUMENT);
		textMap.put("vcs", FileType.DOCUMENT);
		textMap.put("vcf", FileType.DOCUMENT);
	}

	// Add all supported code types
	static {
		codeMap.put("css", FileType.CODE);
		codeMap.put("htm", FileType.CODE);
		codeMap.put("html", FileType.CODE);
		codeMap.put("ts", FileType.CODE);
		codeMap.put("phps", FileType.CODE);
		codeMap.put("tsv", FileType.CODE);
		codeMap.put("xml", FileType.CODE);
		codeMap.put("bib", FileType.CODE);
		codeMap.put("boo", FileType.CODE);
		codeMap.put("h++", FileType.CODE);
		codeMap.put("hpp", FileType.CODE);
		codeMap.put("hxx", FileType.CODE);
		codeMap.put("hh", FileType.CODE);
		codeMap.put("c++", FileType.CODE);
		codeMap.put("cpp", FileType.CODE);
		codeMap.put("cxx", FileType.CODE);
		codeMap.put("h", FileType.CODE);
		codeMap.put("htc", FileType.CODE);
		codeMap.put("csh", FileType.CODE);
		codeMap.put("c", FileType.CODE);
		codeMap.put("d", FileType.CODE);
		codeMap.put("hs", FileType.CODE);
		codeMap.put("java", FileType.CODE);
		codeMap.put("lhs", FileType.CODE);
		codeMap.put("moc", FileType.CODE);
		codeMap.put("p", FileType.CODE);
		codeMap.put("pas", FileType.CODE);
		codeMap.put("gcd", FileType.CODE);
	}

	// Add all supported audio types
	static {
		audioMap.put("3gpp", FileType.AUDIO);
		audioMap.put("amr", FileType.AUDIO);
		audioMap.put("snd", FileType.AUDIO);
		audioMap.put("mid", FileType.AUDIO);
		audioMap.put("midi", FileType.AUDIO);
		audioMap.put("kar", FileType.AUDIO);
		audioMap.put("xmf", FileType.AUDIO);
		audioMap.put("mxmf", FileType.AUDIO);
		audioMap.put("mpga", FileType.AUDIO);
		audioMap.put("mpega", FileType.AUDIO);
		audioMap.put("mp2", FileType.AUDIO);
		audioMap.put("mp3", FileType.AUDIO);
		audioMap.put("m4a", FileType.AUDIO);
		audioMap.put("m3u", FileType.AUDIO);
		audioMap.put("sid", FileType.AUDIO);
		audioMap.put("aif", FileType.AUDIO);
		audioMap.put("aiff", FileType.AUDIO);
		audioMap.put("aifc", FileType.AUDIO);
		audioMap.put("gsm", FileType.AUDIO);
		audioMap.put("m3u", FileType.AUDIO);
		audioMap.put("wma", FileType.AUDIO);
		audioMap.put("wax", FileType.AUDIO);
		audioMap.put("ra", FileType.AUDIO);
		audioMap.put("rm", FileType.AUDIO);
		audioMap.put("ram", FileType.AUDIO);
		audioMap.put("ra", FileType.AUDIO);
		audioMap.put("pls", FileType.AUDIO);
		audioMap.put("sd2", FileType.AUDIO);
		audioMap.put("wav", FileType.AUDIO);
	}

	// Add all supported image types
	static {
		imageMap.put("bmp", FileType.IMAGE);
		imageMap.put("gif", FileType.IMAGE);
		imageMap.put("cur", FileType.IMAGE);
		imageMap.put("ico", FileType.IMAGE);
		imageMap.put("ief", FileType.IMAGE);
		imageMap.put("jpeg", FileType.IMAGE);
		imageMap.put("jpg", FileType.IMAGE);
		imageMap.put("jpe", FileType.IMAGE);
		imageMap.put("pcx", FileType.IMAGE);
		imageMap.put("png", FileType.IMAGE);
		imageMap.put("svg", FileType.IMAGE);
		imageMap.put("svgz", FileType.IMAGE);
		imageMap.put("tiff", FileType.IMAGE);
		imageMap.put("tif", FileType.IMAGE);
		imageMap.put("djvu", FileType.IMAGE);
		imageMap.put("djv", FileType.IMAGE);
		imageMap.put("wbmp", FileType.IMAGE);
		imageMap.put("ras", FileType.IMAGE);
		imageMap.put("cdr", FileType.IMAGE);
		imageMap.put("pat", FileType.IMAGE);
		imageMap.put("cdt", FileType.IMAGE);
		imageMap.put("cpt", FileType.IMAGE);
		imageMap.put("ico", FileType.IMAGE);
		imageMap.put("art", FileType.IMAGE);
		imageMap.put("jng", FileType.IMAGE);
		imageMap.put("bmp", FileType.IMAGE);
		imageMap.put("psd", FileType.IMAGE);
		imageMap.put("pnm", FileType.IMAGE);
		imageMap.put("pbm", FileType.IMAGE);
		imageMap.put("pgm", FileType.IMAGE);
		imageMap.put("ppm", FileType.IMAGE);
		imageMap.put("rgb", FileType.IMAGE);
		imageMap.put("xbm", FileType.IMAGE);
		imageMap.put("xpm", FileType.IMAGE);
		imageMap.put("xwd", FileType.IMAGE);
	}

	// Add all supported video types
	static {
		videoMap.put("3gpp", FileType.VIDEO);
		videoMap.put("3gp", FileType.VIDEO);
		videoMap.put("3g2", FileType.VIDEO);
		videoMap.put("dl", FileType.VIDEO);
		videoMap.put("dif", FileType.VIDEO);
		videoMap.put("dv", FileType.VIDEO);
		videoMap.put("fli", FileType.VIDEO);
		videoMap.put("m4v", FileType.VIDEO);
		videoMap.put("mpeg", FileType.VIDEO);
		videoMap.put("mpg", FileType.VIDEO);
		videoMap.put("mpe", FileType.VIDEO);
		videoMap.put("mp4", FileType.VIDEO);
		videoMap.put("VOB", FileType.VIDEO);
		videoMap.put("qt", FileType.VIDEO);
		videoMap.put("mov", FileType.VIDEO);
		videoMap.put("mxu", FileType.VIDEO);
		videoMap.put("lsf", FileType.VIDEO);
		videoMap.put("lsx", FileType.VIDEO);
		videoMap.put("mng", FileType.VIDEO);
		videoMap.put("asf", FileType.VIDEO);
		videoMap.put("asx", FileType.VIDEO);
		videoMap.put("wm", FileType.VIDEO);
		videoMap.put("wmv", FileType.VIDEO);
		videoMap.put("wmx", FileType.VIDEO);
		videoMap.put("wvx", FileType.VIDEO);
		videoMap.put("avi", FileType.VIDEO);
		videoMap.put("movie", FileType.VIDEO);
	}

	// Add all supported archive types
	static {
		archiveMap.put("001", FileType.ARCHIVE);
		archiveMap.put("7z", FileType.ARCHIVE);
		archiveMap.put("arj", FileType.ARCHIVE);
		archiveMap.put("bin", FileType.ARCHIVE);
		archiveMap.put("bzip", FileType.ARCHIVE);
		archiveMap.put("bzip2", FileType.ARCHIVE);
		archiveMap.put("cab", FileType.ARCHIVE);
		archiveMap.put("cpio", FileType.ARCHIVE);
		archiveMap.put("deb", FileType.ARCHIVE);
		archiveMap.put("ear", FileType.ARCHIVE);
		archiveMap.put("gz", FileType.ARCHIVE);
		archiveMap.put("hqx", FileType.ARCHIVE);
		archiveMap.put("jar", FileType.ARCHIVE);
		archiveMap.put("lha", FileType.ARCHIVE);
		archiveMap.put("rar", FileType.ARCHIVE);
		archiveMap.put("rpm", FileType.ARCHIVE);
		archiveMap.put("sea", FileType.ARCHIVE);
		archiveMap.put("sit", FileType.ARCHIVE);
		archiveMap.put("tar", FileType.ARCHIVE);
		archiveMap.put("war", FileType.ARCHIVE);
		archiveMap.put("zip", FileType.ARCHIVE);
	}

	// Add all supported pdf types
	static {
		pdfMap.put("pdf", FileType.PDF);
	}

	/**
	 * This method will return return file type from file path or url. Extension
	 * is retrieve using apacheFilenameUtils.getExtension(). If not found will
	 * return as a file
	 * 
	 * @param path
	 *            is the file path
	 * @return FileType type
	 */
	public static FileType getFileType(String path) {
		String ext = FilenameUtils.getExtension(path);

		if (textMap.containsKey(ext))
			return FileType.DOCUMENT;
		else if (codeMap.containsKey(ext))
			return FileType.CODE;
		else if (audioMap.containsKey(ext))
			return FileType.AUDIO;
		else if (imageMap.containsKey(ext))
			return FileType.IMAGE;
		else if (videoMap.containsKey(ext))
			return FileType.VIDEO;
		else if (archiveMap.containsKey(ext))
			return FileType.ARCHIVE;
		else if (pdfMap.containsKey(ext))
			return FileType.PDF;
		else
			return FileType.FILE;
	}

	/**
	 * This method will get res id based on the file type. This will internally
	 * call getFileType(path) to get type
	 * 
	 * @param path
	 *            is the file path
	 * @return res id
	 */
	public static int getFileTypeBasedRes(String path) {
		FileType fileType = FileUtils.getFileType(path);

		switch (fileType) {
		case DOCUMENT:
			return R.drawable.ic_icon_document_light;
		case CODE:
			return R.drawable.ic_icon_code_light;
		case AUDIO:
			return R.drawable.ic_icon_audio_light;
		case IMAGE:
			return R.drawable.ic_icon_image_light;
		case VIDEO:
			return R.drawable.ic_icon_video_light;
		case ARCHIVE:
			return R.drawable.ic_icon_archive_light;
		case PDF:
			return R.drawable.ic_icon_pdf_light;
		default:
			return R.drawable.ic_icon_file_light;
		}
	}

	/**
	 * Convenience method to get local path. If default path exist, will create
	 * new one with long time otherwise just the default path
	 * 
	 * @param root
	 *            is the root directory
	 * @param url
	 *            is the download url. This will be used to get name nad the
	 *            extension
	 * @return file
	 */
	public static String getLocalDownloadPath(String root, String url) {
		StringBuilder pathBuilder = new StringBuilder("").append(root)
				.append("/").append(FilenameUtils.getName(url));

		if (new File(pathBuilder.toString()).exists()) {
			StringBuilder pathBuilderWithDate = new StringBuilder("")
					.append(root).append("/")
					.append(FilenameUtils.getBaseName(url)).append("_")
					.append(new Date().getTime()).append(".")
					.append(FilenameUtils.getExtension(url));

			return pathBuilderWithDate.toString();
		} else {
			return pathBuilder.toString();
		}
	}

	/**
	 * This method will check if the external storage is writitable. This method
	 * checks if the storage is mounted
	 * 
	 * @return true is RW otherwise false
	 */
	public static boolean isStorageWritable() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}

		return false;
	}

	/**
	 * This method will return available storage in the external storage
	 * 
	 * @return
	 */
	public static long getAvailableStorageInBytes() {
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				return (long) stat.getAvailableBlocksLong()
						* (long) stat.getBlockSizeLong();
			} else {
				@SuppressWarnings("deprecation")
				Long availableSpace = (long) stat.getAvailableBlocks()
						* (long) stat.getBlockSize();

				return availableSpace;
			}

		} catch (RuntimeException ex) {
			Log.e(TAG, "Error while retrieving stats.", ex);

			return -1L;
		}
	}

	/**
	 * This method will return available storage in given size. If not the type
	 * it will return in bytes
	 * 
	 * @param size
	 *            is the StorageSize type
	 * @return size
	 */
	public static long getAvailableStorage(StorageSize size) {
		long availableStorageInBytes = getAvailableStorageInBytes();

		switch (size) {
		case KB:
			return availableStorageInBytes / SIZE_KB;
		case MB:
			return availableStorageInBytes / SIZE_MB;
		case GB:
			return availableStorageInBytes / SIZE_GB;
		default:
			return availableStorageInBytes;
		}
	}

	/**
	 * This method will return is storage is available. This will internally
	 * call isStorageSpaceAvailable(FileUtils.STORAGE_TRESHOLD,size);
	 * 
	 * @param sizeToWrite
	 *            requested
	 * @return true is available otherwise false
	 */
	public static boolean isStorageSpaceAvailable(long sizeToWrite) {
		return isStorageSpaceAvailable(STORAGE_THRESHOLD, sizeToWrite);
	}

	/**
	 * This method will return is storage is available
	 * 
	 * @treshold is a treshold in percentage of allowed writable. This will
	 *           always ensure that the given threshold is available
	 * @param sizeToWrite
	 *            requested
	 * @return true is available otherwise false
	 */
	public static boolean isStorageSpaceAvailable(long treshold,
			long sizeToWrite) {
		if (getAvailableStorageInBytes() < (sizeToWrite + ((treshold / 100) * sizeToWrite)))
			return false;
		else
			return true;
	}

}
