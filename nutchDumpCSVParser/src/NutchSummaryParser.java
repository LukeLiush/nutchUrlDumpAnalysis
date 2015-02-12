import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.config.ServiceLoader;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.CloseShieldInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class NutchSummaryParser extends TXTParser {
	private static final Set<MediaType> SUPPORTED_TYPES = Collections
			.singleton(MediaType.TEXT_PLAIN);
	private static final ServiceLoader LOADER = new ServiceLoader(
			TXTParser.class.getClassLoader());

	private Map<String, NutchUnfetchedURLEntry> summaries;

	private static final String SEP = "[,|;]";
	private static final Pattern PATT = Pattern.compile(new StringBuffer(
			"(\".*\")").append(SEP).append("(\\d*)").append(SEP)
			.append("(\".*\")").append(SEP).append("(.*)").append(SEP)
			.append("(.*)").append(SEP).append("(\\d*)").append(SEP)
			.append("(.*)").append(SEP).append("(.*)").append(SEP)
			.append("(.*)").append(SEP).append("(\".*\")").append(SEP)
			.append("(\".*\").*").toString());

	// private static final Pattern PATT1 = Pattern
	// .compile("(\".*\")[,|;](\\d*)[,|;](\".*\")[,|;](.*)[,|;](.*)[,|;](\\d*)[,|;](.*)[,|;](.*)[,|;](.*)[,|;](\".*\")[,|;](\".*\").*");
	private static final String TR = "tr";
	private static final String TD = "td";
	private static final String ID = "id";
	private static final String UL = "ul";
	private static final String LI = "li";

	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return SUPPORTED_TYPES;
	}

	public void parse(InputStream stream, ContentHandler handler,
			Metadata metadata, ParseContext context) throws IOException,
			SAXException, TikaException {
		// Automatically detect the character encoding
		AutoDetectReader reader = new AutoDetectReader(
				new CloseShieldInputStream(stream), metadata, context.get(
						ServiceLoader.class, LOADER));
		try {
			Charset charset = reader.getCharset();
			//MediaType type = new MediaType(MediaType.TEXT_PLAIN, charset);
			MediaType type = new MediaType(MediaType.application("json"), charset);
			metadata.set(Metadata.CONTENT_TYPE, type.toString());
			// deprecated, see TIKA-431
			metadata.set(Metadata.CONTENT_ENCODING, charset.name());

			summaries = new HashMap<String, NutchUnfetchedURLEntry>();

			String thisLine = reader.readLine();
			processHeader(thisLine);
			// int i = summaries.size();
			while ((thisLine = reader.readLine()) != null) {
				processLine(thisLine);
				// if (i-- == 0) {
				// break;
				// }

			}

			XHTMLContentHandler xhtml = new XHTMLContentHandler(handler,
					metadata);
			xhtml.startDocument();
			populateXHTML(xhtml);
			xhtml.endDocument();
		} finally {
			reader.close();
		}
	}

	private void populateXHTML(final XHTMLContentHandler xhtml) {
		Iterator<String> keyiter = summaries.keySet().iterator();
		try {
			xhtml.startElement("table border=true");
			while (keyiter.hasNext()) {
				String key = keyiter.next();
				NutchUnfetchedURLEntry entry = summaries.get(key);

				processRow(xhtml, entry);

			}
			xhtml.endElement("table");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processRow(final XHTMLContentHandler xhtml,
			final NutchUnfetchedURLEntry entry) throws SAXException {

		xhtml.startElement(TR);

		xhtml.startElement(TD, ID, "category");
		xhtml.characters(entry.cate);
		xhtml.endElement(TD);

		xhtml.startElement(TD, ID, "count");
		xhtml.characters(Integer.toString(entry.count));
		xhtml.endElement(TD);

		if (!entry.examples.isEmpty()) {
			xhtml.startElement(TD, ID, "examples");
			xhtml.startElement(UL);
			for (String exUrl : entry.examples) {
				xhtml.startElement(LI);
				xhtml.characters(exUrl);
				xhtml.endElement(LI);
			}
			xhtml.endElement(UL);
			xhtml.endElement(TD);
		}

		xhtml.startElement(TD, ID, "isDynamic");
		xhtml.characters(Boolean.toString(entry.isDynamic));
		xhtml.endElement(TD);

		xhtml.endElement(TR);
	}

	public List<NutchUnfetchedURLEntry> getSortList() {
		List<NutchUnfetchedURLEntry> sortList = new LinkedList<NutchUnfetchedURLEntry>();
		Iterator<String> keyiter = summaries.keySet().iterator();
		while (keyiter.hasNext()) {
			String key = keyiter.next();
			sortList.add(summaries.get(key));
		}
		Collections.sort(sortList);
		for (NutchUnfetchedURLEntry entry : sortList) {
			System.out.println(entry);
		}
		return sortList;

	}

	private void processHeader(final String header) {
		// System.out.println(header.split(",").length);
	}

	private void processLine(final String line) {

		// Create a Pattern object
		Matcher matcher = PATT.matcher(line);
		if (matcher.find()) {
			String url = matcher.group(1);
			String statusCode = matcher.group(2);
			// String statuName = matcher.group(3);
			// String fetchTIme = matcher.group(4);
			// String modifyTime = matcher.group(5);
			// String retriesSinceFetch = matcher.group(6);
			// String retryIntervalSec = matcher.group(7);
			// String retryIntervalDay = matcher.group(8);
			// String score = matcher.group(9);
			// String signature = matcher.group(10);
			// String metadata = matcher.group(11);

			short statuCode = Short.parseShort(statusCode);
			switch (statuCode) {
			case 1: // db_unfetched
				// status code = 1 means db_unfetched
				int qmarIdx = url.indexOf('?');// question mark index
				if (qmarIdx > 0) {// dynamic urls
					String category = url.substring(1, qmarIdx + 1);
					if (summaries.containsKey(category)) {
						NutchUnfetchedURLEntry entry = summaries.get(category);
						entry.increment();
						entry.addExample(url);
					} else {
						summaries.put(category, new NutchUnfetchedURLEntry(
								category, true));
					}
				} else {// static urls
					int lstSlhIdx = url.lastIndexOf('/'); // last slash index
					String category = url.substring(1, lstSlhIdx + 1);
					if (summaries.containsKey(category)) {
						NutchUnfetchedURLEntry entry = summaries.get(category);
						entry.increment();
						entry.addExample(url);
					} else {
						summaries.put(category, new NutchUnfetchedURLEntry(
								category, false));
					}
					// System.out.println(url);
				}
				break;
			case 2: // db_fetched
				break;
			}

		}

	}

	static class NutchUnfetchedURLEntry implements Comparable {
		private List<String> examples;
		private boolean isDynamic;
		private int count;
		private String cate;// category
		private static final int EXAMPLES_SIZE = 10;

		NutchUnfetchedURLEntry(final String category, final boolean isDynamic) {
			examples = new LinkedList<String>();
			this.isDynamic = isDynamic;
			count = 1;
			this.cate = category;
		}

		void addExample(final String url) {
			if (this.examples.size() < 10) {
				this.examples.add(url);
			}
		}

		void increment() {
			count = count + 1;
		}

		int getCount() {
			return this.count;
		}

		public String toString() {
			StringBuffer sbuf = new StringBuffer();
			sbuf.append(this.cate).append(",\t count: ").append(count)
					.append(", \t\t\t isDynamic: ").append(this.isDynamic);
			return sbuf.toString();
		}

		public int compareTo(Object o) {
			if (o instanceof NutchUnfetchedURLEntry) {
				NutchUnfetchedURLEntry newO = (NutchUnfetchedURLEntry) o;
				if (newO.count > this.count) {
					return 1;
				} else if (newO.count < this.count) {
					return -1;
				}
			}
			return 0;
		}
	}
}
