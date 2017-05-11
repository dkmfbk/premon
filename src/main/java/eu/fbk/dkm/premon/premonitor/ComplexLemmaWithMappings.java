package eu.fbk.dkm.premon.premonitor;

/**
 * Created by alessio on 09/05/17.
 */

public class ComplexLemmaWithMappings {

    ComplexLemma lemma;
    String framenet;
    String vn;
    String rolesetID;
    String pbSource;

    public String getPbSource() {
        return pbSource;
    }

    public void setPbSource(String pbSource) {
        this.pbSource = pbSource;
    }

    public ComplexLemmaWithMappings(ComplexLemma lemma) {
        this.lemma = lemma;
    }

    public String getRolesetID() {
        return rolesetID;
    }

    public void setRolesetID(String rolesetID) {
        this.rolesetID = rolesetID;
    }

    public String getFramenet() {
        return framenet;
    }

    public void setFramenet(String framenet) {
        this.framenet = framenet;
    }

    public String getVn() {
        return vn;
    }

    public void setVn(String vn) {
        this.vn = vn;
    }

    public ComplexLemma getLemma() {
        return lemma;
    }
}
