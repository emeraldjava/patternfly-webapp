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

<#macro writeSortJavaScript dataTable dataTableResultId>
    <#assign id = dataTable.id! />
    <script language="javascript">
        $(document).ready(function() {
            $('#${dataTableResultId} tr').click(function(event) {
            	console.log($(event.target));
                var target = $(event.target);
                if (target.get(0).tagName != "TH")
                    target = target.parentsUntil("th");
                var sens_tri = target.attr('dt_sens_tri');
                if (sens_tri !== undefined && sens_tri >= 0) {
                    sens_tri = 3 - (sens_tri == 0 ? 2 : sens_tri);
                    var mode_tri = target.attr('dt_mode_tri');
                    fctWaiting(true);
                    document.location.href = "${dataTable.sortLink}&modeTri${id!}=" + mode_tri + "&sensTri${id!}=" + sens_tri;
                }
            });
        });
    </script>
</#macro>

<#function pageLink dataTable>
    <#assign id = dataTable.id />
    <#return dataTable.sortLink
        ?replace("&pageIndex" + (id!) + "=" + dataTable.pageIndex, "")
        ?replace("?pageIndex" + (id!) + "=" + dataTable.pageIndex + "&", "?")
        + "&modeTri" + (id!) + "=" + dataTable.modeTri
        + "&sensTri" + (id!) + "=" + dataTable.sensTri
        + "&pageIndex" + (id!) + "=" />
</#function>

