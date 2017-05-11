//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2015.05.07 alle 12:32:49 PM CEST 
//

package eu.fbk.dkm.premon.premonitor.propbank;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "", propOrder = {
        "value" }) @XmlRootElement(name = "alias") public class Alias {

    @XmlValue protected String value;
    @XmlAttribute(name = "framenet") @XmlJavaTypeAdapter(NormalizedStringAdapter.class) protected String framenet;
    @XmlAttribute(name = "pos") @XmlJavaTypeAdapter(NormalizedStringAdapter.class) protected String pos;
    @XmlAttribute(name = "verbnet") @XmlJavaTypeAdapter(NormalizedStringAdapter.class) protected String verbnet;

    /**
     * Recupera il valore della proprietà value.
     *
     * @return possible object is
     * {@link String }
     */
    public String getvalue() {
        return value;
    }

    /**
     * Imposta il valore della proprietà value.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setvalue(String value) {
        this.value = value;
    }

    public String getFramenet() {
        return framenet;
    }

    public String getPos() {
        return pos;
    }

    public String getVerbnet() {
        return verbnet;
    }
}
