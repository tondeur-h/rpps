package com.ht.dev.rpps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller
public class RppsApplication {
    
    @Autowired
    JdbcTemplate jdbctemplate;
    
    List<Medecin> medecins=new ArrayList<>();
    List<RPPSFull> rppsLst=new ArrayList<>();
    
    
	public static void main(String[] args) {
		SpringApplication.run(RppsApplication.class, args);
	}
        
@RequestMapping (value="/",method=RequestMethod.GET)
private String index(Model model){
    model.addAttribute("error", Boolean.FALSE);
    return "index";
}
  
@RequestMapping (value="/recherche",method=RequestMethod.POST)
//mapper les valeurs
private String recherche(@RequestParam(value="rppstxt", required=false, defaultValue="") String rppstxt,@RequestParam(value="nomtxt", required=false, defaultValue="") String nomtxt,@RequestParam(value="prenomtxt", required=false, defaultValue="") String prenomtxt,@RequestParam(value="villetxt", required=false, defaultValue="") String villetxt,@RequestParam(value="professiontxt", required=false, defaultValue="") String professiontxt,@RequestParam(value="raisonsocialetxt", required=false, defaultValue="") String raisonsocialetxt,Model model){
    //tester au moins une valeur est renseignée
    if (rppstxt.isEmpty() && nomtxt.isEmpty() && prenomtxt.isEmpty() && professiontxt.isEmpty() && villetxt.isEmpty() && raisonsocialetxt.isEmpty()){
        //rediriger vers index
        model.addAttribute("error", Boolean.TRUE);
        return "index";
    }
    
    
    //requeter
    //construire SQL
String sql="SELECT * FROM rpps.rpps WHERE Identifiant_PP like '%"+rppstxt+"%' AND Libelle_profession like '%"+professiontxt+"%' " +
"AND Raison_sociale_site like '%"+raisonsocialetxt+"%' AND Nom_d_exercice like '%"+nomtxt+"%' AND Prenom_d_exercice like '%"+prenomtxt+"%' " +
"AND (Bureau_cedex like '%"+villetxt+"%' OR Libelle_commune like '%"+villetxt+"%') order by Nom_d_exercice, Prenom_d_exercice";
    //System.out.println(sql);
    //vider avant de remplir pour une nouvelle requête...
    medecins.clear();
    List<Map<String,Object>> rows =jdbctemplate.queryForList(sql);
    rows.stream().map((row) -> {
        Medecin med=new Medecin();
        med.setIdentifiant_PP(row.get("Identifiant_PP").toString());
        med.setNom_d_exercice(row.get("Nom_d_exercice").toString());
        med.setPrenom_d_exercice(row.get("Prenom_d_exercice").toString());
        med.setRaison_sociale_site(row.get("Raison_sociale_site").toString());
        med.setLibelle_commune(row.get("Libelle_commune").toString());
        med.setLibelle_savoir_faire(row.get("Libelle_savoir_faire").toString());
        med.setAdresse_e_mail(row.get("Adresse_e_mail").toString());
        med.setTelephone(row.get("Telephone").toString());
            return med;
        }).forEachOrdered((med) -> {
            medecins.add(med);
        });
    // afficher les résultats
    model.addAttribute("medecinsLst", medecins);
    return "recherche";
}


@RequestMapping (value="/detail",method=RequestMethod.GET)
private String detail(@RequestParam(value="rpps", required=true) String rppstxt,Model model){
    
String sql="SELECT concat_ws('<br>',Identifiant_PP,Raison_sociale_site) as RF," +
"concat_ws(' ','<strong>Nom:</strong>',Libelle_civilite_exercice,Nom_d_exercice,Prenom_d_exercice,'<br>'," +
"'<strong>profession:</strong>(',Code_profession,')',Libelle_profession,'<br>'," +
"'<strong>savoir faire:</strong>(',Code_savoir_faire,')',Libelle_savoir_faire,'<br>'," +
"'<strong>SIRET:</strong>',Numero_SIRET_site,'<br>'," +
"'<strong>SIREN:</strong>',Numero_SIREN_site,'<br>'," + 
"'<strong>FINESS:</strong>',Numero_FINESS_site,'<br>'," +
"'<strong>FINESS Etb</strong>:',Numero_FINESS_etablissement_juridique,'<br>'," +
"'<strong>Raison sociale:</strong>',Raison_sociale_site,'<br>',"+
"'<strong>Enseigne:</strong>',Enseigne_commerciale_site,'<br>'," +
"'<strong>Structure:</strong>(',Identifiant_structure,')', Complement_destinataire, Complement_point_geographique,'<br>'," +
"'<strong>Adresse:</strong>',Numero_Voie,Indice_repetition_voie,Code_type_de_voie,Libelle_type_de_voie,Libelle_Voie," +
"Mention_distribution,Bureau_cedex,Code_postal,'(commune=',Code_commune,')', Libelle_commune,Libelle_pays,'<br>'," +
"'<strong>Tel:</strong>',Telephone,'/',Telephone_2,'Fax=',Telecopie,'<br>',"+
"'<strong>Email:</strong>',Adresse_e_mail,'<br>',"+
"'<strong>MSSante:</strong>',Adresse_BAL_MSSante) as Details" +
" FROM rpps.rpps WHERE Identifiant_PP='"+rppstxt+"'";
    
    rppsLst.clear();
    List<Map<String,Object>> rows =jdbctemplate.queryForList(sql);
    rows.stream().map((row) -> {
        RPPSFull rf=new RPPSFull();
        rf.setRpps(row.get("RF").toString());
        rf.setDetails(row.get("Details").toString());
            return rf;
        }).forEachOrdered((rf) -> {
            rppsLst.add(rf);
        });
    model.addAttribute("rpps", rppstxt);
    model.addAttribute("rppsLst", rppsLst);
 
    return "detail";
}

}