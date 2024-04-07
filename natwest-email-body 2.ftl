<html>
<body>
<table>
    <#list formData.data.fieldNames() as k>
        <tr class="tableBody">
            <td>${k}</td>
            <td>${formData.data.get(k)}</td>
        </tr>
    </#list>
</table>
</body>
</html>