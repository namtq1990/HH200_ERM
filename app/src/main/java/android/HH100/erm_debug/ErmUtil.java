package android.HH100.erm_debug;

import android.HH100.MainActivity;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class ErmUtil {

    public static void saveErmXml(Context context, Spectrum[] spectrums) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputStream parentTemplate = context.getAssets().open("parent_template.xml");
        InputStream childrenTemplate = context.getAssets().open("children_template.xml");
        FileOutputStream fout  = new FileOutputStream("/sdcard/test.xml");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document parentDoc = builder.parse(parentTemplate);
            Element childNode = (Element) builder.parse(childrenTemplate).getFirstChild();
            Node parentNode = parentDoc.getElementsByTagName("NucareQueryResponse").item(0);

            for (int i = 0; i < spectrums.length; i++) {
                Spectrum spc = spectrums[i];
                boolean hasSpectrum = spc.hasSpectra();
                Element child = (Element) parentDoc.importNode(childNode, true);
                child.setAttribute("time", spc.Get_MesurementDate());
                child.setAttribute("doserate", String.format("%.1f", spc.mGammaDoserate));
                child.setAttribute("isSpectrum", String.valueOf(hasSpectrum));
                if (hasSpectrum) {
                    child.setAttribute("acqTime", String.valueOf(spc.Get_AcqTime()));
                } else {
                    child.removeAttribute("acqTime");
                }

                Element spcElement = (Element) child.getElementsByTagName("spectrum").item(0);
                if (hasSpectrum) {
                    double[] params = spc.Get_Coefficients().get_Coefficients();
                    if (params == null) {
//                    int[] temp = NcLibrary.GetTextCli(MainActivity.FilenameCaliInfo, 3);
                        params = new double[]{
                                0, 0, 0
                        };
                    }

                    spcElement.setAttribute("calibration", String.format("%f %f %f", params[0], params[1], params[2]));
                    if (hasSpectrum) {
                        spcElement.setTextContent(spc.ToString());
                    } else {
                        spcElement.setTextContent("");
                    }
                } else {
                    child.removeChild(spcElement);
                    child.setTextContent("");
                }

                parentNode.appendChild(child);
            }

            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(parentDoc), new StreamResult(fout));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parentTemplate.close();
            childrenTemplate.close();
            fout.close();
        }
    }
}
