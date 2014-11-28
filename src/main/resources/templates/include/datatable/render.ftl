<#macro showDataTable dataTable>

<#local id=dataTable.id />

<#local resultsPerPage=dataTable.resultsPerPage!10 />
<#local showFilters = "" />
<#if dataTable.isShowFilters()??>
    <#if dataTable.isShowFilters()>
        <#local showFilters = "1" />
    <#else>
        <#local showFilters = "0" />
    </#if>
</#if>

<!-- Un espace pour un problème de DIV -->
&nbsp;
<script language="javascript">

    function doExport${id}(format) {
        var rForm = document.getElementById('formRecherche${id}');

        rForm.modeExport${id}.value = format;
        rForm.submit();
    }

    function doSubmit${id}() {
        fctWaiting(true);

        var rForm = document.getElementById('formRecherche${id}');

        rForm.modeExport${id}.value = "${dataTable.modeExport!}";
        rForm.submit();
    }

    function doReset${id}() {
        $(document).ready(':input','#formRecherche${id}')
            .not(':button, :submit, :reset, :hidden')
            .val('')
            .attr('value', '')
            .removeAttr('checked')
            .removeAttr('selected');
    }
</script>
<#--
<span style="float: right; font-style: italic; font-size: 0.8em"><a href="javascript: showDataTableVersion('flexigrid')">Afficher la version dynamique</a></span>
-->

    <div class="dataTable_wrapper form-inline">
    
    <form method="get" action="" name="formRecherche${id}" id="formRecherche${id}">
    <input type="hidden" value="${dataTable.modeTri}" name="modeTri${id}" id="modeTri${id}">
    <input type="hidden" value="${dataTable.sensTri}" name="sensTri${id}" id="sensTri${id}">
    <input type="hidden" value="${dataTable.pageIndex}" name="pageIndex${id}" id="pageIndex${id}"/>
    <input type="hidden" value="${dataTable.modeExport!}" name="modeExport${id}" id="modeExport${id}">
    <input type="hidden" value="${showFilters}" name="showFilters${id}" id="showFilters${id}">

    <#if (dataTable.params?size > 0)>
        <#list dataTable.params?keys as paramName>
            <input type="hidden" value="${dataTable.params[paramName]!}" name="${paramName}" id="${paramName}">
        </#list>
    </#if>

    <#assign hasFilter=false>
    <#if (dataTable.filters?size > 0)>
        <#list dataTable.filters as filter>
            <#if !filter.hidden>
                <#assign hasFilter=true>
            </#if>
        </#list>
    </#if>

    <#assign filterVisible = false>
    <#if showFilters == "" || showFilters == "1">
        <#assign filterVisible = true>
    </#if>
    
    <script language="javascript">
        $(document).ready(function() {
            $('#btnReset${id}').click(function () {
                doReset${id}();
            });

            $('#btnSearch${id}').click(function () {
                doSubmit${id}();
            });
        });

        function switchFiltrage${id}() {
            if ($('#dataTableSearchBarExtended${id}').is(":visible")) {
                $('#dataTableSearchBarTitleIcon${id}').removeClass('fa-angle-up').addClass('fa-angle-down');
                $('#dataTableSearchBarExtended${id}').slideUp("fast");
                $('#dataTableSearchBarSearchButton${id}').fadeIn("fast");
                $('#showFilters${id}').val("0");
            } else {
                $('#dataTableSearchBarTitleIcon${id}').removeClass('fa-angle-down').addClass('fa-angle-up');
                $('#dataTableSearchBarExtended${id}').slideDown("fast");
                $('#dataTableSearchBarSearchButton${id}').fadeOut("fast");
                $('#showFilters${id}').val("1");
            }
        }

        <#if (!hasFilter) && dataTable.results?size = 0>
            $(function() {
                $('#dataTableSearchBarSearchButton${id}').show();
            });
        </#if>
    </script>

    <#-- Affichage des filtres -->
    <#if hasFilter || (dataTable.results?size = 0)>
    <div id="dataTableSearchBarTitle${id}" style="display: visible" class="row">

        <div class="col-md-6">
            <#if hasFilter>
                <span style="font-weight: bold"><a href="javascript: switchFiltrage${id}()"><@spring.message "datatable.filter.block" /> <span id="dataTableSearchBarTitleIcon${id}" class="fa fa-angle-<#if filterVisible>up<#else>down</#if>"></span></a></span>
            </#if>
        </div>
        <div class="col-md-6" style="min-height: 27px">
            <#if dataTable.availableExporters??>
                <#if (dataTable.results?size > 0)>
                    <#if ((","+dataTable.availableExporters+",")?index_of("csv") >= 0) >
                        <a href="javascript:doExport${id}('csv')" class="btn btn-default btn-xs pull-right" style="margin-top: 0px; margin-bottom: 5px"><img src="../rsrc-bo/images/export_xls.png" align="absmiddle" border="0"> Exporter</a>
                    </#if>
                </#if>
            </#if>
            <div id="dataTableSearchBarSearchButton" style="display: <#if filterVisible>none<#else>visible</#if>">
                <a href="javascript:doSubmit${id}()" class="btn btn-default btn-xs pull-right" style="margin-top: 0px; margin-bottom: 5px; margin-right: 5px"><@spring.message "datatable.filter.button" /></a>
            </div>
        </div>
    </div>
    </#if>

    <#if hasFilter>
        <#-- Comptage nb de lignes et nb de colonnes -->
        <#assign filterRowCount = 1>
        <#assign filterColumnCount = 0>
        <#assign filterTmpColumnCount = 0>
        <#list dataTable.filters as filter>
            <#if !filter.hidden>
                <#if filter.newLine>
                    <#assign filterRowCount = filterRowCount + 1>
                    <#assign filterTmpColumnCount = 0>
                </#if>
                <#assign filterTmpColumnCount = filterTmpColumnCount + filter.colSpan>
                <#if (filterTmpColumnCount > filterColumnCount)>
                    <#assign filterColumnCount = filterTmpColumnCount>
                </#if>
            </#if>
        </#list>
        <#if filterColumnCount = 0>
            <#assign filterColumnCount = dataTable.filters?size>
        </#if>

        <div class="dataTables_header">

        <div id="dataTableSearchBarExtended${id}" style="display: <#if filterVisible>visible<#else>none</#if>">

        <table cellspacing="0" cellpadding="0" border="0" style="width: 100%; margin: 0; margin-bottom: 0px">
            <tbody>
                <tr>
                    <#local filterSubmitWritten = false>
                    <#local filterTmpRowCount = 0>
                    <#local filterTmpColumnCount = 0>
                    <#local btnSearchWritten = false>
                    <#list dataTable.filters as filter>
                        <#if !filter.hidden>
                            <#local filterTmpColumnCount = filterTmpColumnCount + filter.colSpan>
            
                            <td <#if filter.width??>style="width:${filter.width}px"</#if>>
                                ${filter.label}&nbsp;:
                            </td>
                            <td colspan="${1 + 2 * (filter.colSpan - 1)}" style="text-align: left">
                            	<#if filter.acceptedOperators??>
                           			<select id="filter_${filter_index + 1}_op${id}" name="filter_${filter_index + 1}_op${id}">
                            		<#list filter.acceptedOperators as filterAcceptedOperator>
                            			<option value="${(filterAcceptedOperator.operator.id)!0}" <#if filter.operator.id == (filterAcceptedOperator.operator.id)!0>selected="selected"</#if>>${filterAcceptedOperator.label!}</option>
                            		</#list>
                           			</select>
                            	</#if>
                                <#if !filter.acceptedValues??>
                                    <label><input type="text" id="filter_${filter_index + 1}${id}" name="filter_${filter_index + 1}${id}" value="${filter.value!?html}" <#if filter.size??>size="${filter.size}"</#if> class="form-control"></label>
                                <#else>
                                    <select id="filter_${filter_index + 1}${id}" name="filter_${filter_index + 1}${id}" style="font-size: 11px; <#if filter.size??>width: ${filter.size}px</#if>" class="form-control">
                                        <option value=""></option>
                                        <#list filter.acceptedValues as filterAcceptedValue>
                                            <option <#if filter.value??><#if filterAcceptedValue.value = filter.value>selected</#if></#if> value="${filterAcceptedValue.value?html}">${filterAcceptedValue.label?html}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </td>
            
                            <#if filterTmpColumnCount = filterColumnCount || !filter_has_next || dataTable.filters[filter_index + 1].newLine>
                                <#-- ecriture TD restants -->
                                <#if filterTmpColumnCount < filterColumnCount>
                                    <td colspan="${(filterColumnCount - filterTmpColumnCount) * 2}">&nbsp;</td>
                                </#if>
                                <#if ((filterTmpColumnCount = filterColumnCount && filterTmpRowCount = 0) || filter_index = 0) && !btnSearchWritten>
                                    <#local btnSearchWritten = true>
                                    <td style="vertical-align: middle; text-align: center" colspan="2" rowspan="${filterRowCount}">
                                        <input id="btnSearch" type="submit" onClick="doSubmit${id}()" value="<@spring.message "datatable.filter.button" />" class="btn btn-default btn-sm" style="width: 80px">
                                    </td>
                                </#if>
                                </tr>
                                <#if filter_has_next>
                                    <tr>
                                </#if>
                                <#local filterTmpColumnCount = 0>
                                <#local filterTmpRowCount = filterTmpRowCount + 1>
                            </#if>
                        </#if>
                    </#list>
            </tbody>
        </table>
        </div>

        </div>
    </#if>

    <#assign resultColCount=0 />

    <table cellpadding="0" cellspacing="0" border="0" class="datatable datable-manual dataTable_result table table-condensed table-striped table-bordered" id="dataTableResult${id}">
        <thead>
            <tr role="row">
                <#if dataTable.viewLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#if dataTable.modifyLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#if dataTable.changeStatusLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#list dataTable.columns as column>
                    <#if !column.hidden>
                        <#local sensTri = -1>
                        <#if column.sortable == true>
                            <#local sensTri = 0>
                            <#if dataTable.modeTri == column_index + 1>
                                <#local sensTri = dataTable.sensTri>
                            </#if>
                        </#if>
                        <#assign resultColCount = resultColCount + 1 />
                        <th dt_sens_tri='${sensTri}' dt_mode_tri='${column_index + 1}' class="<#switch sensTri><#case 0>sorting<#break><#case 1>sorting_asc<#break><#case 2>sorting_desc<#break></#switch>"
                            style="<#if ((column.width!0) > 0)>width: ${column.width!0}px;</#if> vertical-align: top">
                            ${column.label}
                            <#if column.headerComment??>
                                <span class="glyphicon glyphicon-info-sign" id="tip-info-dt${dataTable.id}-${column_index}" title="${column.headerComment}"></span>
                            </#if>
                        </th>
                    </#if>
                </#list>
                <#if dataTable.deleteLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#--
                <th style="width: 0px">Actions</th>
                -->
            </tr>
        </thead>
        <tbody>
            <#if dataTable.results?size == 0>
                <tr>
                    <td colspan="${resultColCount}">
                        <#if dataTable.firstView!false>
                            &nbsp;
                        <#else>
                            Aucun résultat
                        </#if>
                    </td>
                </tr>
            </#if>
            <#list dataTable.results as result>
                <tr>
                    <#if dataTable.viewLink??>
                        <td style="text-align: center">
                            <#if dataTable.viewLink?? && (!result.canBeViewed?? || result.canBeViewed == true)>
                                <a href="${replace(dataTable.viewLink, result)}" title="visualiser"><img src="../rsrc-bo/images/preview.gif"></a>
                            </#if>
                        </td>
                    </#if>
                    <#if dataTable.modifyLink??>
                        <td style="text-align: center">
                            <#if dataTable.modifyLink?? && (!result.canBeModified?? || result.canBeModified == true)>
                                <a href="${replace(dataTable.modifyLink, result)}" title="modifier"><img src="../rsrc-bo/images/edit2.png"></a>
                            </#if>
                        </td>
                    </#if>
                    <#if dataTable.changeStatusLink??>
                        <td style="text-align: center">
                            <#if result.rawCodeStatus??>
                                <#switch (result.rawCodeStatus?string)>
                                    <#case "0">
                                        <a href="${replaceCodeStatus(dataTable.changeStatusLink, result)}" title="Activer"><img src="../rsrc-bo/images/up2.gif"></a>
                                        <#break>
                                    <#case "1">
                                        <a href="${replaceCodeStatus(dataTable.changeStatusLink, result)}" title="Désactiver"><img src="../rsrc-bo/images/down2.gif"></a>
                                        <#break>
                                </#switch>
                            </#if>
                        </td>
                    </#if>
                    <#list dataTable.columns as column>
                        <#if !column.hidden>
                            <td class='dtr${dataTable.id}-${(column.getId()!"")?replace(".", "_")}'>
                                ${getResultValueByColumn(result, column)}
                            </td>
                        </#if>
                    </#list>
                    <#if dataTable.deleteLink??>
                        <td style="text-align: center">
                            <#if dataTable.deleteLink?? && (!result.canBeDeleted?? || result.canBeDeleted == true)>
                                <a href="${replace(dataTable.deleteLink, result)}" title="supprimer"><img src="../rsrc-bo/images/trash.gif"></a>
                            </#if>
                        </td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>
<div class="dataTables_footer" style="margin-top: -20px">
	<#assign viewCount1 = 1 + (dataTable.pageIndex -1) * resultsPerPage />

    <#if (dataTable.pageIndex * resultsPerPage > dataTable.resultCount)>
        <#assign viewCount2 = dataTable.resultCount />
    <#else>
        <#assign viewCount2 = (dataTable.pageIndex) * resultsPerPage />
    </#if>

    <#assign viewCount3 = dataTable.resultCount!0 />

	&nbsp; <@message "datatable.results.view.label" viewCount1 viewCount2 viewCount3 />

    <#if dataTable.resultRealCount ?? && (dataTable.resultRealCount > dataTable.resultCount)><img
        src="../rsrc-bo/images/error2.png" style="margin-top: -4px" data-placement="right" data-toggle="tooltip"
        title="Trop de résultats (${dataTable.resultRealCount}). Veuillez affiner le filtrage."
        id="img-too-many-results-${dataTable.id}">
        <script language="javascript"> $(document).ready(function() { $("#img-too-many-results-${dataTable.id}").tooltip(); }); </script></#if>

    <div class="dataTables_paginate paging_bootstrap_input" id="DataTables_Table_0_paginate">
        <ul class="pagination">
            <#if (dataTable.pageIndex == 1)>
                <li class="first disabled">
                    <span class="i fa fa-angle-double-left"></span>
                </li>
                <li class="prev disabled">
                    <span class="i fa fa-angle-left"></span>
                </li>
            <#else>
                <#assign pagLink = pageLink(dataTable) />
                <li class="first disabled">
                    <span class="i fa fa-angle-double-left"></span>
                </li>
                <li class="prev">
                    <span class="i fa fa-angle-left"></span>
                </li>
            </#if>
        </ul>

        <div class="pagination-input">
        	<#-- 
            <input type="text" class="paginate_input"><span class="paginate_of_no_js"><@spring.message "of" /> <b>${dataTable.pageCount}</b></span>
            -->
            <input type="text" class="paginate_input"><span class="paginate_of_no_js"><@spring.message "of" /> <b>${dataTable.pageCount}</b></span>
        </div>
        
        <ul class="pagination">
            <li class="next <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>"><span class="i fa fa-angle-right"></span></li>
            <li class="last <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>"><span class="i fa fa-angle-double-right"></span></li>
        </ul>
    </div>
</div>

    <#if (dataTable.results?size > 0)>
    <div id="dataTablePager${id}" class="row" style="height: 50px">
        <div class="col-md-6">
            
        </div>
        <div class="col-md-6">
            <div class="dataTables_paginate pull-right" style="margin: 0px">
                <#if resultsPerPage?? && (dataTable.pageCount > 1)>
                    <#assign pagLink = pageLink(dataTable) />
                    <#assign pagCount = 5 />
                    <ul class="pagination pagination-sm" style="margin: 0">
                        <#-- Bouton "Précédent" -->
                        <#if (dataTable.pageIndex == 1)>
                            <li class="prev disabled"><a href="#">&laquo;</a></li>
                        <#else>
                            <li class="prev"><a href="${pagLink}${(dataTable.pageIndex - 1)?c}">&laquo;</a></li>
                        </#if>
                        

                        <#-- Bouton "Premier" -->
                        <#if (dataTable.pageIndex == 1)>
                            <li class="prev disabled"><a href="#">&laquo;</a></li>
                        <#else>
                            <li class="prev"><a href="${pagLink}1" title="Premier">&laquo;</a></li>
                        </#if>
    
                        <#if (dataTable.pageCount > pagCount)>
                            <#local currentCount=0 />
                            <#list (dataTable.pageIndex + 1 - (max(pagCount - (dataTable.pageCount - dataTable.pageIndex), 3)))..(dataTable.pageIndex - 1) as i>
                                <#if (i > 0)>
                                    <li><a href="${pagLink}${i?c}">${i?c}</a></li>
                                    <#local currentCount=currentCount+1 />
                                </#if>
                            </#list>
                            <li class="active"><a href="#">${dataTable.pageIndex?c}</a></li>
                            <#list (dataTable.pageIndex + 1)..(dataTable.pageIndex + (pagCount - currentCount) - 1) as i>
                                <#if (i != dataTable.pageIndex) && (i <= dataTable.pageCount)>
                                    <li><a href="${pagLink}${i?c}">${i?c}</a></li>
                                </#if>
                            </#list>
                        <#else>
                            <#list 1..dataTable.pageCount as i>
                                <li <#if dataTable.pageIndex = i>class="active"</#if>><a href="${pagLink}${i?c}">${i?c}</a></li>
                            </#list>
                        </#if>
                        
                        <#-- Bouton "Suivant" -->
                        <#if (dataTable.pageIndex >= dataTable.pageCount)>
                            <li class="next disabled">
                                <a href="#">&raquo;</a>
                            </li>
                        <#else>
                            <li class="next <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>">
                                <a href="${pagLink}${(dataTable.pageIndex + 1)?c}">&raquo;</a>
                            </li>
                        </#if>
                        

                        <#-- Bouton "Dernier" -->
                        <#if (dataTable.pageIndex >= dataTable.pageCount)>
                            <li class="next disabled">
                                <a href="#">&raquo;</a>
                            </li>
                        <#else>
                            <li class="next <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>">
                                <a href="${pagLink}${dataTable.pageCount?c}" title="Dernier">&raquo;</a>
                            </li>
                        </#if>
                    </ul>
                </#if>
            </div>
        </div>
    </div>
    </#if><#-- pager -->
    
    </form>
    </div>

    <#assign dataTableResultName = "dataTableResult" />
    <#if id?has_content>
        <#assign dataTableResultName = dataTableResultName + id />
    </#if>
    <@writeSortJavaScript dataTable dataTableResultName />

</#macro>
