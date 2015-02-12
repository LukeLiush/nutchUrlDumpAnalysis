import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.SAXException;

public class NutchDumpSummaryMain {
	private File csvDumpPath, outputPath;

	public NutchDumpSummaryMain(final Builder builder) {
		csvDumpPath = new File(builder.csvDumpPath);
		outputPath = new File(builder.outputPath);
	}

	public void process() {
		NutchSummaryParser par = new NutchSummaryParser();
		Metadata metadata = new Metadata();
		ParseContext context = new ParseContext();
		
		InputStream in_stream;
		try {
			//ToHTMLContentHandler
			ToHTMLContentHandler handler = new ToHTMLContentHandler(new FileOutputStream(outputPath), "UTF-8");
			//NutchDumpToJSonHandler handler = new NutchDumpToJSonHandler(new FileOutputStream(outputPath), "UTF-8");
			in_stream = TikaInputStream.get(csvDumpPath, metadata);
			par.parse(in_stream, handler, metadata, context);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static class Builder {
		private String csvDumpPath, outputPath;

		public synchronized Builder csvDumpath(final String csvDumpPath) {
			this.csvDumpPath = csvDumpPath;
			return this;
		}

		public synchronized Builder outputPath(final String outputPath) {
			this.outputPath = outputPath;
			return this;
		}
	}

	public static void main(String args[]) {

		String nutchCSVDumpPath = "part-00000_acadis.csv";
		String summaryOutputPath = "acadis.htm";
		NutchDumpSummaryMain main = new NutchDumpSummaryMain(new Builder()
				.csvDumpath(nutchCSVDumpPath).outputPath(summaryOutputPath));
		main.process();
	}
}
