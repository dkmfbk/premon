//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.15 at 02:35:27 PM CET 
//


package eu.fbk.dkm.premon.premonitor.verbnet;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "synrestrsOrSELRESTRS"
})
@XmlRootElement(name = "NP")
public class NP {

    @XmlAttribute(name = "value", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String value;
    @XmlElements({
        @XmlElement(name = "SYNRESTRS", required = true, type = SYNRESTRS.class),
        @XmlElement(name = "SELRESTRS", required = true, type = SELRESTRS.class)
    })
    protected List<Object> synrestrsOrSELRESTRS;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the synrestrsOrSELRESTRS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the synrestrsOrSELRESTRS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSYNRESTRSOrSELRESTRS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SYNRESTRS }
     * {@link SELRESTRS }
     * 
     * 
     */
    public List<Object> getSYNRESTRSOrSELRESTRS() {
        if (synrestrsOrSELRESTRS == null) {
            synrestrsOrSELRESTRS = new ArrayList<Object>();
        }
        return this.synrestrsOrSELRESTRS;
    }

}
