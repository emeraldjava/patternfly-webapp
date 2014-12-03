<#import "/layout/layouts.ftl" as layout>

<@layout.main>
  <h1>Static rendering (<a href="/json${navigatorDataTable.sortLink}">JSON</a>)</h1>

  <@layout.showDataTable navigatorDataTable />
  
  <h1>Static rendering with Ajax <em>(not functionnal)</em></h1>

  <@layout.showDataTable navigatorDataTableAjax 'ajax' />

  <h1>Dynamic rendering</h1>
  
  <table class="datatable table table-striped table-bordered">
    <thead>
      <tr>
        <#list navigatorDataTableAll.columns as column>
            <th>${column.label}</th>
        </#list>
      </tr>
    </thead>
    <tbody>
        <#list navigatorDataTableAll.results as result>
            <tr>
                <#list navigatorDataTableAll.columns as column>
                    <td>${layout.getResultValueByColumn(result, column)}</td>
                </#list>
            </tr>
        </#list>
    </tbody>
  </table>

</@layout.main>