package fr.pgervaise.patternfly.datatable.domain;

/**
 * Une interface pour préciser qu'une classe possède un identifiant
 * @author Philippe Gervaise
 *
 */
public interface ICodable<T> {
	public T getCode();
	public void setCode(T code);
}
