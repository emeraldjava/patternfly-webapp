package fr.pgervaise.patternfly.datatable.domain;

/**
 * Gère un statut où "1" signifie actif, 0 "inactif"
 * @author pgervaise
 *
 */
public interface IStatuable {

	public String getCodeStatut();
	public void setCodeStatut(String codeStatut);

}
