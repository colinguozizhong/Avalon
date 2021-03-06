package avalon.extend

import org.eclipse.jetty.util.UrlEncoded
import org.jdom2.Element
import org.jdom2.xpath.XPathFactory

import scala.collection.mutable.ArrayBuffer

/**
	* Created by Eldath Ray on 2017/6/11 0011.
	*
	* @author Eldath Ray
	*/
object WolframXMLParser {

	class WolframPod(val titleString: String, val idString: String, val plaintextString: String) {
		def title: String = UrlEncoded.decodeString(titleString)

		def id: String = idString

		def plaintext: String = plaintextString
	}

	def get(root: Element): ArrayBuffer[WolframPod] = {
		val xPath = XPathFactory.instance
		val objects = xPath.compile("//subpod").diagnose(root, false)
		val result = ArrayBuffer.newBuilder[WolframPod]
		objects.getResult.forEach((e: Any) => {
			val e1 = e.asInstanceOf[Element]
			result += new WolframPod(
				handleString(e1.getAttributeValue("title")),
				handleString(e1.getAttributeValue("id")),
				handleString(e1.getChild("plaintext").getValue))
		})
		result.result()
	}

	private def handleString(i: String): String = {
		if (i == null)
			return ""
		i.replaceAll(" +", " ")
	}
}