package edu.cmu.lti.qalab.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class ExtractAlzheimerFiles {

	public static void main(String args[]) {
		try {
			ExtractAlzheimerFiles main=new ExtractAlzheimerFiles();
			File gzFile=new File(
					"/media/alkesh/My Passport/QA4MRE/PMC/pmc/00/00/BMC_Genomics_2013_Jan_21_14(Suppl_1)_S11.tar.gz");
			//InputStream gzipStream = new GZIPInputStream(fileStream);
			File outputDir=new File("/media/alkesh/My Passport/QA4MRE/PMC/OnlyNXMLs/");
			File tempDir=new File("/media/alkesh/My Passport/QA4MRE/PMC/Temp/");
			File tarFile=main.unGzip(gzFile,tempDir);
			System.out.println(tarFile);
			List<File>files=main.unTar(tarFile, outputDir);
			for(int i=0;i<files.size();i++){
				System.out.println(files.get(i).getAbsolutePath());
			}
			//tempDir.delete();
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Untar an input file into an output file.

	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension. 
	 * 
	 * @param inputFile     the input .tar file
	 * @param outputDir     the output directory file. 
	 * @throws IOException 
	 * @throws FileNotFoundException
	 *  
	 * @return  The {@link List} of {@link File}s with the untared content.
	 * @throws ArchiveException 
	 */
	private List<File> unTar(final File inputFile,final File outputDir) throws FileNotFoundException, IOException, ArchiveException {

	    System.out.println(String.format("Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

	    final List<File> untaredFiles = new LinkedList<File>();
	    final InputStream is = new FileInputStream(inputFile); 
	    final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
	    TarArchiveEntry entry = null; 
	    while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
	    	
	    	System.out.println(entry.getFile().getName());
	    	final File outputFile = new File(outputDir, entry.getName());
	        
	        if (entry.isDirectory()) {
	        	
	            System.out.println(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
	            if (!outputFile.exists()) {
	                System.out.println(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
	                if (!outputFile.mkdirs()) {
	                    throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
	                }
	            }
	        } else {
	            System.out.println(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
	            final OutputStream outputFileStream = new FileOutputStream(outputFile); 
	            IOUtils.copy(debInputStream, outputFileStream);
	            outputFileStream.close();
	        }
	        untaredFiles.add(outputFile);
	    }
	    debInputStream.close(); 

	    return untaredFiles;
	}

	/**
	 * Ungzip an input file into an output file.
	 * <p>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.gz' extension. 
	 * 
	 * @param inputFile     the input .gz file
	 * @param outputDir     the output directory file. 
	 * @throws IOException 
	 * @throws FileNotFoundException
	 *  
	 * @return  The {@File} with the ungzipped content.
	 */
	private  File unGzip(final File inputFile, final File outputDir) throws FileNotFoundException, IOException {

	    System.out.println(String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

	    final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

	    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
	    final FileOutputStream out = new FileOutputStream(outputFile);

	    for (int c = in.read(); c != -1; c = in.read()) {
	        out.write(c);
	    }

	    in.close();
	    out.close();

	    return outputFile;
	}

}
