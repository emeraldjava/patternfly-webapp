package fr.pgervaise.patternfly.datatable.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.springframework.ui.Model;

import fr.pgervaise.patternfly.config.Constants;
import fr.pgervaise.patternfly.datatable.core.DataTableResultMapper;
import fr.pgervaise.patternfly.datatable.domain.IIdentifiable;
import fr.pgervaise.patternfly.datatable.domain.IStatuable;
import fr.pgervaise.patternfly.datatable.service.ValeurReferenceService;

/**
 * 
 * @author Philippe Gervaise
 *
 * @param <B> Bean
 */
public abstract class AbstractView<B> implements ViewViewable, ViewDeletable {

	protected ViewMetaData viewMetaData = new ViewMetaData();
	protected B bean = null;
	protected DataTableResultMapper<?> mapper;
	protected String iconBar;

	/**
	 * 
	 * @param bean
	 */
	public AbstractView(B bean) {
		this.bean = bean;

		/*
		if (bean instanceof IStatuable && Constants.CODE_STATUT_INACTIF_S.equals(((IStatuable) bean).getCodeStatut())) {
			// style = "class='inactif'";
			viewMetaData = new ViewMetaData();
			viewMetaData.setStyleClass("data-inactif");
		} */
		
		iconBar = "&nbsp;";
	}

	/**
	 * 
	 */
	public void init() {
		
	}

	/**
	 * 
	 * @return
	 */
	public DataTableResultMapper<?> getMapper() {
		return mapper;
	}

	/**
	 * 
	 * @param mapper
	 */
	public void setMapper(DataTableResultMapper<?> mapper) {
		this.mapper = mapper;
	}

	/**
	 * Récupère les meta données de la vue
	 * @return
	 */
	public ViewMetaData getViewMetaData() {
		return viewMetaData;
	}

	/**
	 * Détermine si l'élément peut être visualisé
	 * Par défaut oui (si la table implémente un lien correspondant)
	 * @return
	 */
	public boolean getCanBeViewed() {
		return true;
	}

	/**
	 * Détermine si l'élement peut être modifié
	 * Par défaut oui (si la table implémente un lien correspondant)
	 * @return
	 */
	public boolean getCanBeModified() {
		return true;
	}

	/**
	 * Détermine si l'élement peut être supprimé
	 * Par défaut oui (si la table implémente un lien correspondant)
	 * @return
	 */
	public boolean getCanBeDeleted() {
		return true;
	}

	/**
	 * Détermine si l'élément peut changer de status
	 * Par défaut oui (si la table implémente un lien correspondant)
	 * @return
	 */
	public boolean canChangeStatus() {
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public B getBean() {
		return bean;
	}

	/**
	 * Retourne le status (0 ou 1) pour "canChangeStatus"
	 */
	public String getRawCodeStatus() {
		if (bean != null) {
			try {
				Method m = bean.getClass().getMethod("getCodeStatut", (Class[]) null);
				Object codeStatut = m.invoke(bean, null);

				if (codeStatut != null) {
					if (codeStatut instanceof String) {
						if (codeStatut.equals(Constants.CODE_STATUT_ACTIF_S) || codeStatut.equals(Constants.CODE_STATUT_INACTIF_S))
							return (String) codeStatut;
					} else if (codeStatut instanceof Number) {
						if (((Number) codeStatut).longValue() == Constants.CODE_STATUT_ACTIF)
							return Constants.CODE_STATUT_ACTIF_S;
						if (((Number) codeStatut).longValue() == Constants.CODE_STATUT_INACTIF)
							return Constants.CODE_STATUT_INACTIF_S;
					}
				}
			} catch (NoSuchMethodException e1) {
				// RAF
			} catch (InvocationTargetException e2) {
				// RAF
			} catch (IllegalAccessException e3) {
				// RAF
			}
		}

		return null;
	}
	
	/**
	 * 
	 * @param o
	 * @return
	 */
	public String javascriptSimpleQuoteProtect(Object o) {
		if (o == null)
			return null;

		return o.toString().replace("'", "\\'");
	}
	
	/**
	 * 
	 * @param imgName
	 * @param aHref
	 * @return
	 */
	public String getIcon(String imgName, String aHref) {
		return getIcon(imgName, aHref, null);
	}

	/**
	 * 
	 * @param imgName
	 * @param aHref
	 * @param aTitle
	 * @return
	 */
	public String getIcon(String imgName, String aHref, String aTitle) {
		String url = "<div class=\"text-center\"><a href=\"" + aHref + "\" " + (aTitle == null ? "" : " title=\"" + aTitle + "\"") + ">" +
			"<img src=\"../rsrc-bo/images/" + imgName + "\" border=\"0\"></a></div>";

		return url;
	}

	/**
	 * 
	 * @return
	 */
	public final String getIconBar() {
		return iconBar;
	}

	/**
	 * 
	 * @param iconBar
	 */
	public final void setIconBar(String iconBar) {
		this.iconBar = iconBar;
	}

	/**
	 * 
	 * @return
	 */
	public String getCodeStatut() {
	    /*
		if (bean instanceof Bean) {
			return ((Bean) bean).isActif() ? "Oui" : "Non";
		} */

		return null;
	}
	
	/**
	 * 
	 * @param domaineReference
	 */
	protected String getCodeStatut(String domaineReference) {
		String codeStatut = null;
		
		if (bean instanceof IStatuable) {
			codeStatut = ((IStatuable) bean).getCodeStatut();
		}

		if (codeStatut == null)
			return null;

		ValeurReferenceService valeurReferenceService = getValeurReferenceService();

		if (valeurReferenceService == null)
			return codeStatut;

		return valeurReferenceService.getLibelle(domaineReference, codeStatut);
	}

	/**
	 * 
	 * @return
	 */
	public Date getDateCreation() {
		/*if (bean instanceof IStatuable) {
			return ((IStatuable) bean).getDateCreation();
		} */

		return null;
	}

	/**
	 * 
	 * @return
	 */
	public final Date getDateModificationStatut() {
		/* if (bean instanceof IStatuable) {
			return ((IStatuable) bean).getDateModificationStatut();
		} */

		return null;
	}

	/**
	 * 
	 * @return
	 */
	public ValeurReferenceService getValeurReferenceService() {
		return (ValeurReferenceService) getMapperParameter("valeurReferenceService");
	}
	
	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return (Model) getMapperParameter("model");
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	private Object getMapperParameter(String name) {
		if (mapper == null)
			return null;

		if (mapper.getParameters() == null)
			return null;

		return mapper.getParameters().get(name);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getSelectionIcon() {
		if (bean instanceof IIdentifiable) {
			String url = "<a href=\"javascript:selection" + bean.getClass().getSimpleName() + "(" + ((IIdentifiable) bean).getId() + ")\">" +
				"<img src=\"../rsrc-bo/images/select.png\" border=\"0\"></a>";
	
			return url;
		}

		return null;
	}
	
}
