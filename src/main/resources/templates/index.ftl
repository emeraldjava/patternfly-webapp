<#include "includes/header.ftl" />
  <h1>Basic</h1>
  <table class="datatable table table-striped table-bordered">
    <thead>
      <tr>
        <th>Rendering engine</th>
        <th>Browser</th>
        <th>Platform(s)</th>
        <th>Engine version</th>
        <th>CSS grade</th>
      </tr>
    </thead>
    <tbody>
        <#list dataArray as datas>
            <tr>
            <#list datas as data>
                <td>${data}</td>
            </#list>
            </tr>
        </#list>      
    </tbody>
  </table>
  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum eget eros tincidunt, semper ante nec, dapibus ante.</p>

<#include "includes/footer.ftl" />
