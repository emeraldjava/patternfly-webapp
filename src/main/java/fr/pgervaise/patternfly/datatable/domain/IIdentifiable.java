package fr.pgervaise.patternfly.datatable.domain;

/**
 * Une interface pour préciser qu'une classe possède un identifiant (numérique)
 * @author Philippe Gervaise
 *
 */
public interface IIdentifiable<T extends Number> {
	public T getId();
	public void setId(T id);
}
