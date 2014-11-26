package fr.pgervaise.patternfly.datatable.datasource;

import java.util.List;

import javax.persistence.EntityManager;

import fr.pgervaise.patternfly.datatable.core.DataTable;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableJPADataSource<V> implements DataTableDataSource<V> {

    private EntityManager entityManager;

    public DataTableJPADataSource(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public List<? extends Object> getResults(DataTable<V> dataTable) {
        return null;
    }
}
