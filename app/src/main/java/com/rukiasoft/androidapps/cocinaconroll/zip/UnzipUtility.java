package com.rukiasoft.androidapps.cocinaconroll.zip;

import android.net.Uri;

import com.rukiasoft.androidapps.cocinaconroll.utilities.LogHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
public class UnzipUtility {
	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;
	private static final java.lang.String TAG = LogHelper.makeLogTag(UnzipUtility.class);

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 */
	public void unzip(String zipFilePath, String destDirectory) {
		int size;
		byte[] buffer = new byte[BUFFER_SIZE];

		try {
			if ( !destDirectory.endsWith("/") ) {
				destDirectory += "/";
			}
			File f = new File(destDirectory);
			if(!f.isDirectory()) {
				f.mkdirs();
			}
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFilePath), BUFFER_SIZE));
			try {
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					String path = destDirectory + ze.getName();
					File unzipFile = new File(path);

					if (ze.isDirectory()) {
						if(!unzipFile.isDirectory()) {
							unzipFile.mkdirs();
						}
					} else {
						// check for and create parent directories if they don't exist
						File parentDir = unzipFile.getParentFile();
						if ( null != parentDir ) {
							if ( !parentDir.isDirectory() ) {
								parentDir.mkdirs();
							}
						}

						// unzip the file
						FileOutputStream out = new FileOutputStream(unzipFile, false);
						BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
						try {
							while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
								fout.write(buffer, 0, size);
							}

							zin.closeEntry();
						}
						finally {
							fout.flush();
							fout.close();
						}
					}
				}
			}
			finally {
				zin.close();
			}
		}
		catch (Exception e) {
			LogHelper.e(TAG, "Unzip exception", e);
		}
	}

	/**
	 * Extracts a zip entry (file entry)
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	public void zip(List<Uri> files, String zipFile) throws IOException {
		BufferedInputStream origin;
		File f = new File(zipFile.substring(0, zipFile.lastIndexOf("/") + 1));
		if(!f.isDirectory()) {
			f.mkdirs();
		}

		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		try {
			byte data[] = new byte[BUFFER_SIZE];

			for (int i = 0; i < files.size(); i++) {
				FileInputStream fi = new FileInputStream(files.get(i).getPath());
				origin = new BufferedInputStream(fi, BUFFER_SIZE);
				try {
					ZipEntry entry = new ZipEntry(files.get(i).getPath().substring(files.get(i).getPath().lastIndexOf("/") + 1));
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
						out.write(data, 0, count);
					}
				}
				finally {
					origin.close();
				}
			}
		}
		finally {
			out.close();
		}
	}
}
