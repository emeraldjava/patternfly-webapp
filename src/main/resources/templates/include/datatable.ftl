<#function max x y>
    <#if (x<y)><#return y><#else><#return x></#if>
</#function>

<#function min x y>
    <#if (x<y)><#return x><#else><#return y></#if>
</#function>

<#macro pages totalPages p>
    <#assign size = totalPages?size>
    <#if (p<=5)> <#-- p among first 5 pages -->
        <#assign interval = 1..(min(5,size))>
    <#elseif ((size-p)<5)> <#-- p among last 5 pages -->
        <#assign interval = (max(1,(size-4)))..size >
    <#else>
        <#assign interval = (p-2)..(p+2)>
    </#if>
    <#if !(interval?seq_contains(1))>
     1 ... <#rt>
    </#if>
    <#list interval as page>
        <#if page=p>
         <${page}> <#t>
        <#else>
         ${page} <#t>
        </#if>
    </#list>
    <#if !(interval?seq_contains(size))>
     ... ${size}<#lt>
    </#if>
</#macro>

<#function replace str result>
    <#if str == "%id%">
        <#return replaceCode(str, result) />
    </#if>

    <#local str = replaceCode(str, result) />

    <#local res = str?matches("%.*?%") />

    <#list res as m>
        <#local propName = m?substring(1, m?length - 1) />
        <#local inlineTemplate =(r"<#assign propVal=result." + propName + "! />")?interpret>
        <@inlineTemplate />
        <#if propVal?? && propVal?is_number>
            <#assign propVal = propVal?c />
        </#if>
        <#local str = str?replace(m, propVal) />
    </#list>

    <#return str />
</#function>

<#function replaceCode str result>
    <#return str?replace("%id%", getCode(result)) />
</#function>

<#function replaceCodeStatus str result>
    <#return replaceCode(str, result)?replace("%codeStatus%", getCodeStatus(result)) />
</#function>

<#function getCode obj>
    <#if obj?? && obj.id??>
        <#if obj.id?is_number>
            <#return obj.id?c?url>
        </#if>  
        <#return obj.id?url>
    </#if>
    <#return "" />
</#function>

<#function getCodeStatus obj>
    <#if obj.rawCodeStatus?is_number>
        <#return obj.rawCodeStatus?c?url>
    </#if>
    <#return obj.rawCodeStatus?url>
</#function>

<#-- Retourne la valeur d'une ligne selon la colonne voulu -->
<#function getResultValueByColumn result column>
    <#local v="label" />
    <#local cid=column.id />
    
    <#if cid?contains('.')>
        <#local cid_point_index=cid?index_of('.') + 1>
        <#local cid=cid?substring(cid_point_index)>
    </#if>
    
    <#local inlineTemplate =(r"<#assign x=result." + cid + "! />")?interpret>

    <@inlineTemplate />

    <#if column.rawView == true>
        <#if x?is_number>
            <#local x=x?c />
        </#if>
    </#if>

    <#if x?is_date>
        <#if column.format??>
            <#local x=x?string(column.format) />
        <#else>
            <#local x=x?string('dd/MM/yyyy HH:mm:ss') />
        </#if>
    </#if>
    
    <#if x?is_string && column.html == false>
        <#local x=x?html />
    </#if>

    <#if x?is_string && x = "">
        <#local x="&nbsp;" />
    </#if>

    <#if result.viewMetaData??>
        <#if result.viewMetaData.styleClass??>
            <#local x = '<span class="' + result.viewMetaData.styleClass + '">' + x + '</span>' />
        </#if>
    </#if>

    <#return x />
</#function>

<#-- Retourne le numéro de la colonne selon son identifiant (-1 si pas trouvé) -->
<#function getColumnIndexById columns id>
    <#list columns as column>
        <#local cid=column.id />

        <#if cid?contains('.') && !id?contains('.')>
            <#local cid_point_index=cid?index_of('.') + 1>
            <#local cid=cid?substring(cid_point_index)>
        </#if>

        <#if cid == id>
            <#return column_index />
        </#if>

    </#list>

    <#return -1 /> 
</#function>

<#macro writeJavaScript dataTable type>
    <#local id = dataTable.id! />
    <script language="javascript">
        function doExport_${id}(format) {
	        var rForm = document.getElementById('formRecherche_${id}');
	
	        rForm.modeExport_${id}.value = format;
	        rForm.submit();
	    }

	    function doSubmit_${id}(type) {
			fctWaiting(true);

	        if (type == 'ajax') {
	        	$.ajax({
	        		url: '?modeExport_${id}=json&pageIndex=1',
	        		success: function(obj) {
	        			console.log(obj);
	        		}
	        	});

	        	fctWaiting(false);
	        } else {
		        var rForm = document.getElementById('formRecherche_${id}');
		
		        rForm.modeExport_${id}.value = "${dataTable.modeExport!}";
		        rForm.submit();
			}
	    }
	    
	    function doReset_${id}() {
	        $(document).ready(':input','#formRecherche_${id}')
	            .not(':button, :submit, :reset, :hidden')
	            .val('')
	            .attr('value', '')
	            .removeAttr('checked')
	            .removeAttr('selected');
	    }

        function switchFiltrage_${id}() {
            if ($('#dataTableSearchBarExtended_${id}').is(":visible")) {
                $('#dataTableSearchBarTitleIcon_${id}').removeClass('fa-angle-up').addClass('fa-angle-down');
                $('#dataTableSearchBarExtended_${id}').slideUp("fast");
                $('#dataTableSearchBarSearchButton_${id}').fadeIn("fast");
                $('#showFilters_${id}').val("0");
            } else {
                $('#dataTableSearchBarTitleIcon_${id}').removeClass('fa-angle-down').addClass('fa-angle-up');
                $('#dataTableSearchBarExtended_${id}').slideDown("fast");
                $('#dataTableSearchBarSearchButton_${id}').fadeOut("fast");
                $('#showFilters_${id}').val("1");
            }
        }

        $(document).ready(function() {
	        // Show filter bar if needed
		    <#local hasFilter = dataTable.hasVisibleFilter()>
	        <#if (!hasFilter) && dataTable.results?size = 0>
				$('#dataTableSearchBarSearchButton_${id}').show();
	        </#if>

        	// Sort
            $('#dataTableResult_${id} tr').click(function(event) {
                var target = $(event.target);
                if (target.get(0).tagName != "TH")
                    target = target.parentsUntil("th");
                var sens_tri = target.attr('dt_sens_tri');
                if (sens_tri !== undefined && sens_tri >= 0) {
                    sens_tri = 3 - (sens_tri == 0 ? 2 : sens_tri);
                    var mode_tri = target.attr('dt_mode_tri');

					$('#modeTri_${id}').val(mode_tri);
					$('#sensTri_${id}').val(sens_tri);
					
                    doSubmit_${id}();
                }
            });

            // Paginate
            $('#DataTables_Table_${id}_paginate').click(function(event) {
            	<#local pagLink = pageLink(dataTable) />
            	var target = $(event.target);
            	if (!target.parent().hasClass('disabled')) {
					var pageIndex = undefined;

					if (target.hasClass('fa-angle-double-left'))
	            		pageIndex = 1;
	            	if (target.hasClass('fa-angle-left'))
	            		pageIndex = ${(dataTable.pageIndex - 1)?c};
	            	if (target.hasClass('fa-angle-right'))
	            		pageIndex = ${(dataTable.pageIndex + 1)?c};
	            	if (target.hasClass('fa-angle-double-right'))
						pageIndex = ${dataTable.pageCount?c};

					if (pageIndex) {
						$('#pageIndex_${id}').val(pageIndex);
						doSubmit_${id}();
					}
				}    		
            });

            // Set current page
            $('#DataTables_Table_${id}_paginate input').val(${dataTable.pageIndex?c}).prop('disabled', true);

            $('#btnReset_${id}').click(function () {
                doReset_${id}();
            });

            $('#btnSearch_${id}').click(function () {
               	doSubmit_${id}('${type}');
            });
        });
    </script>
</#macro>


<#function pageLink dataTable>
    <#local id = dataTable.id />
    <#return dataTable.sortLink
        ?replace("&pageIndex_" + (id!) + "=" + dataTable.pageIndex, "")
        ?replace("?pageIndex_" + (id!) + "=" + dataTable.pageIndex + "&", "?")
        + "&modeTri_" + (id!) + "=" + dataTable.modeTri
        + "&sensTri_" + (id!) + "=" + dataTable.sensTri
        + "&pageIndex_" + (id!) + "=" />
</#function>

