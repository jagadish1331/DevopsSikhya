<!DOCTYPE html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:o="urn:schemas-microsoft-com:office:office">
<#assign body=JsonUtil.jsonToMap(formData.data)>
<#assign steps=JsonUtil.getSteps(formDefinition.stepDefinitions)>
<#setting date_format="dd-MM-yyyy">
<#assign createdOn=formData.dateCreated>
<html>
    <head>
        <title>testJp</title>
        <style>
            .elementName {
                color: grey;
                display: flex;
    
            }
    
            .elementValue {
                background-color: #F4F4F7;
                margin-right: 5px;
                padding-left: 10px;
                padding: 2px;
    
            }
    
            hr {
                display: block;
                width: 650px;
                height: 1px;
                border: 0;
                border-top: 1px solid #ccc;
                margin: 1em 0;
                padding: 0;
            }
    
            #navbar {
                width: 100vw;
                height: 40px;
                border: 3px solid white;
                border-radius: 15px;
                background-color: #42145F
            }
    
            #navbar img {
                display: block;
                width: 40px;
                height: 40px;
                margin: auto;
    
            }
    
            .summaryElemName {
                color: grey;
                display: flex;
            }
    
            .summaryElemValue {
                background-color: #F4F4F7;
                padding-left: -50px;
                padding: 0px;
    
            }
            .centerimg{
                display:block;
                margin-left:auto;
                margin-right:auto;
                width:50%;
            }
        </style>
    </head>
    <body>
        <center>
        <#switch formData.brand>
            <#case "rbs">
            <header>
                <table bgcolor="#002d64" style="width:650px;background-color:#002d64;height:10%; font-family: RNHouseSans,Arial,sans-serif;color: white;font-size: 1.5rem;">
                    <tr>
                        <td style="padding-left: 250px;margin-top:5px">
                            <img style="margin-left:-15px;margin-top:5px;margin-right:5px;height:40px;width:200px;object-fit:scale-down;image-resolution:172dpi;"
                            src="data:image/png;base64,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"
                            alt="RBS" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <h1 style="margin-top:5px;text-align: center">testJp</h1>
                        </td>
                    </tr>
                </table>
            </header>
            <#break>
            <#case "ubn">
            <header>
                <table style="width:650px;background-color:white;height:10%; font-family: RNHouseSans,Arial,sans-serif;color: white;font-size: 1.5rem;">
                    <tr>
                        <td style="margin-top:5px;padding-left:200px">
                            <img class="centerimg" style="margin-top:5px;height:40px;width:200px;object-fit:scale-down;image-resolution:172dpi;color:white;"
                            src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPwAAAAgCAYAAAA2VxUzAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAArMSURBVHhe7VzLjR03EHQGhgGt4ZtD0MHWGj45BIeguyE9HX1UCAphQ3AICkEhKIQNQe5qNkccsoqf2bfap4ctoIDVdLO72ezmcGae/UPGzavT6xe3p/cv/ji9++m3f3+Myw1e/P72L9cz/vLnP7/G5R/wd74OnbjcALbhw/x9GPkqUfotWcYwixVbTK83v8cA4vL1+cZ+n3GlsCK+u7l99yXT/v2JNSKKrtS7uT3doxi9IO3vncx0Y9gGb3azXel9DHEXaLTduOCRJlixRfVsfIgfDT/fvv3bN8VXp8/f0u9ToczvFK1uLB93qLPZm8b3ClWvIV5DatbWmDlpmh7XGj27S9OAqkamzR5kd9YaatLX2vBP5fepwOY7z9M9cnOtja/qNcRruLl985IZA83RrumFjh9zG1nR8L1mT3zzMlQl1KSfG/46wOa7StTYNTa9qtcQrwOJYgbBMolC3m34UbNDBr0RqA+Mf274qwCb7xGinq6t6VW9hngdM00JHSGTDT9rN8LogvqAjeeGvwrQ+b46fcacC+KZ/WP5XoPx2vKE+bB5hvgYZppTXKcND/2RvdlmB5gPt/Pc8FcBNt/6PVAJrFXzonjj6T7UrgKqXkN8HKOmZ0QwKiBF+FhpdkD5eG746wCbb6/hAZNXX41Kjt8LfS9Q9Rrih2G16RGMCogRtlebHVA+LrHhPYf4evHq9B/m29iwQvajafjDVwrYLNmMiXG1nnOQgxyP+20/nyKWD1MvTs0P8x/iPI+78sgdoiF2MWVabCGWUHf52brwmDGvijNfjlbBfIWoC+SYzTHED8dK0+fAmawmbB5pdkBNejZpJVZsUb2iyGso24zZzmz+FM8Wj21QvfXhttLxOW0atewbNLxvYu1YVRfY2L5ufu24Pf1z392oxqitInb7+7V+75B8rOdd59b80bUAQ6XdffyHH2L3TEzfPmP4YFLg6R42Sx8rxy41adgJlWms2KJ6xbwzVjbJzGwn5YLrzPBc8SRizfm6wE+jj2KfKbAB2FjYDrGEqlG2lt7oRHeKNscw08Bz0OoPX1qXRO+opqd5N4Z4B/MrH3Mwf1dSBiVt8qvBKW5BDKDsXkrDIydMt8ds5zEa3q4daPZEjA0zO8BPq69vCsoOAxuPpgkxhc4bf2nH458nxoepHRAn0e2+tKYU81Vxh3gDNmq5AdopwpXQuExBcaZBLfDOy5SWavMooSZ9CQ2P0xHTA00XC7975k0F8vWEdO6Gdx9EL3ze4aTlpy37m+u1NgFtV3DQsCVWxw+KmzZmOrW2+itkz/ZpPbn+OtvTlcp7iB290wSubz22VGwLC4jnQWqDcKZp1aQvoeHVUdH00q4qkIvHF8t8l1T2aj2wLEK9+fCjumocHDFDZYPK245WI9BzXdv4Y+gQylaIN/icPQ7R7L2jsY2tdPFS1V96Zg43QnLDm2l4xGV6HyI3Z9loQ+zQNk/3TT5U8lrOP3OnQmI2as59M1WTxiKFyjRWbFG9alGUPXY3mAWzV/tlQFHRsZ08qQ2rXm81z40LDV6D2lsm39Qy0maYTlZqU8iQm7htEqGyYdTw5q/Z+GXO2SYn8h7iwYma5APFMGz6A4uZAtV2fdeb3ETUpC+54XG3CJVlMHu1X4aU02osKaISKP5mjLG+m6l5gjOx9cBsrhCNOGpiYEYng9YuySWuNXpBy4s85TF9Zl/lHbJ0YxU99oAN+MmhJn3JDe93kwPxAcxe7beGPM5bEWFsj2wcrodph9LDPFcaiYHbXaPF1/28tQrayEsN38/LrH2Vd9imG7wRuYjhLbBLqIGZ9eLPwIK3o8b13+HTLtvqbbRFXL3bMzujNUD8bNxR1v5k3sgxdxXM7jGi3uZqaoTZhqR6SVd+ygNm7ffyTq/fDr6O9Jqy5EpzzRff9/8MD8hFL4gNDmNn7kJ0PPFbQs3rKGt/yv4orhlQu5Evwrt+zc6fOKAXL+rS1xNfx45t0pBq7Ue1SccR+x5brSc5mPvw7lSSBKOgksA407Rq0pfS8H68mv7uagU1eL5i45jfEmpeR1n7U/aPrEENZndUb735mqz7hQQxqztklyQmVeshlqDjiP2VdR2uBQqVDVRknyVq6Le+nDGsi3MWm05gexRkehgf4h2i6eUnl4adIx/TV34zZI6wEaG41rnblKT9J2p4QMWETTVUdvA1OtLomSQmzxXRDbEEHUfs6zm2NN3uRucwJ0s/lDF9+ku7KPjp4JyTbxLPWWzKVoh3YHoYH2IKvDxD4tnYmip+qjvwe84cMTymfWaXFX+N3g2rjivq8/AvEJ0X3vDOmZ7yIrUEZaZfJfWfk0rDKai+vj8rFT5WvlPDF7M7c+KoQRNtDPEOTA9zDXEXeQPEsyizA+JuE+o7UN2BX5Wjh3weLKEKD2sZKofB7GKdQtyFxcV/YVbFpfRArANqCWPySY/WCYlppZ5KzNqXeRfX0WsrvbW0EyJBaXPg8pKwCdvhZglpIajN8RGmAtuY0JQh3qHWc91B4zEgTmYLsYTKDkx35FflyIqo+7Z4FvDP7NeNdQTMLit+BrWhlnGZLX7DsEZXNUntXlDDh4xvdnbdB4+weuxBMCogRtg+0vQYw+yhYVbsyYUXG4fQXW54QBVmiHdgeirGjHPlSEGt81M2vPztgbGMC43dyMUmD8iX2RfW8N2X7qONHkVhxqebHUQwKiBF+DhSgCo2LGaodJGSwx871LGX6WK+IV4Cj3/pDj/ctWWODpyEamDe1PYTNjyKmo41hoqDbbaYT4gbsA3CeWENDyg5KB/nRs0OGZPDGXOo9DMhW216S4Z8uQh7anLxEs1i5M3e3+mJvtkK8Yb0ZUL/4EPGLoqa6hrrOWJu5fNaN0dWxL1nO88TnmMtH+zdSMohsfsEDY/aMblsdot1t8HN6GT0cshiwjWmG2IJOo7YV3kPscN0RK+R091Ms0eCmwARDA3IdGftRhhT6NnLdB3E6vH2XiQmyl3QwPQx3xA70Chf5ebP/KK5oOd/92K24gozO6TYiT5hHQ8alultTLnJ/+VWirEaU9sEXLfQ2XQfqeF944kYS6bc9Ne13tjkZl9sbL7h2SbA9Daa71DfkOJpdUMsQccR+z7nWs8YYsfgaL+3qQIGzdnWlExvW4TqenYyano2wR56x/JDHDznsDGYb4gdKHimNyLyEiYaIC42hrGOp7v4kzSbzd0PfqjuY93hD5KeTtQRfZWkXnGN6YZYgo4j9lXeQ7xB6YFbjezvTo3S7g7MAoQh6qgIfNT09W48wrmafktCBzPjbK76CCiY7qj6EaC3LjXZPFJMD8jRQuFdUsOzZgeOrBHNH8kLrjV6xhBL0HELeQ/xDioW0NcJzUiFVbMDzBiCoQFVgfeavvYzg7A3/6u2kj6Puf/Ago3HfEPsoPPvEHHPzNninCrSOp4M3xg7BaC5//8VZqh5XkTD2zxHcSDvdCwhdNl8sVGHuQ0qxyGWoOPsWog3qLyHeId0oxCPLzn2OhH2b/psjd2z1INhOGBO2HMxa3r4DvEhuE2LC0e22nbh45PLTW/1NGFjt2fHjVVhobFw3YuOxYDrR/2bL7OJ4qvsWr6Tv/H/VbWIjxUDCiFsve++z8AGBBs1JzfPHuqYZug58Uefef9Jn9sDkYucA88Z0XFDBVIOxno16Di7FuINs3FktH2a+e7L/6Wgc3hICDMWAAAAAElFTkSuQmCC"
                            alt="ubn" />  
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <h1 style="margin-top:5px;text-align: center;color:#002d64;">testJp</h1>
                        </td>
                    </tr>
                </table>
            </header>
            <#break>
            <#case "ulsterbank">
            <header>
                <table style="width:650px;background-color:white;height:10%; font-family: RNHouseSans,Arial,sans-serif;color: white;font-size: 1.5rem;">
                    <tr>
                        <td style="margin-top:5px;padding-left: 200px">
                            <img class="centerimg" style="margin-top:5px;height:40px;width:200px;object-fit:scale-down;image-resolution:172dpi;color:white;"
                            src="data:image/png;base64,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"
                            alt="ulsterbank" />  
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <h1 style="margin-top:5px;text-align: center;color:#002d64;">testJp</h1>
                        </td>
                    </tr>
                </table>
            </header>
            <#break>
            <#default>
            <header>
                <table cellpadding="0" cellspacing="0" border="0" role="presentation" bgcolor="#42145f" style="width:650px;background:#42145f;color:white;border:1px solid-grey;font-family:Calibri;font-size: 12px">
                    <tr>
                        <td style="padding-left: 300px;margin-top:5px">
                            <img src="data:image/png;base64,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"
                            alt="nwb" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <h1 style="margin-top:5px;text-align: center">testJp</h1>
                        </td>
                    </tr>
                </table>
            </header>
        </#switch>
            <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                        style="width:650px; height:10%; font-family:Calibri;color:black;font-size: 16px">
                        <tr>
                            <td>
                                <p><br /><br />Reference: <b>${formData.uuid}</b>
                                </p>
                                <p>Received: <b>${createdOn?datetime.iso?string("dd-MM-yyyy HH:mm")}</b>
                                </p>
                            </td>
                        </tr>
                    </table>
                    <table cellpadding="5" cellspacing="5" border="0" role="presentation"
                        style="width:650px; height:10%; font-family:Calibri;color:black;font-size: 14px">
                        <colgroup>
                            <col span="1">
                            <col style="background-color:#F4F4F7;">
                        </colgroup>
                        <tr>
                            <td>
                                <p style="color: #2196f3;;display:flex;font-size:24px">Summary</p>
                            </td>
                        </tr>
<tr>
                            <td>
                                <p><strong>About You:</strong></p>
                            </td>
                        </tr>
<#if body.newStringFieldName??>
<tr>
                            <td>
                                <p class="elementName">Name</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.newStringFieldName}</p>
                            </td>
                        </tr>

</#if>
<#if body.newStringFieldFname??>
<tr>
                            <td>
                                <p class="elementName">Father Name</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.newStringFieldFname?}</p>
                            </td>
                        </tr>

</#if>
<#if body.dateFieldDOB??>
<tr>
                            <td>
                                <p class="elementName">Date Of Birth</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.dateFieldDOB}</p>
                            </td>
                        </tr>

</#if>
<#if body.newStringFieldPlace??>
<tr>
                            <td>
                                <p class="elementName">Place</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.newStringFieldPlace}</p>
                            </td>
                        </tr>

</#if>

<#if body.newStringFieldMailID??>
<tr>
                            <td>
                                <p class="elementName">Mail ID</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.newStringFieldMailID}</p>
                            </td>
                        </tr>

</#if>

<#if body.newStringFieldAC??>
<tr>
                            <td>
                                <p class="elementName">Account No</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.newStringFieldAC}</p>
                            </td>
                        </tr>

</#if>
<#if body.feedbackcomment3??>
<tr>
                            <td>
                                <p class="elementName">Feedback</p>
                            </td>
                            <td>
                                <p class="elementValue"> ${body.feedbackcomment3}</p>
                            </td>
                        </tr>

</#if>

                    </table>
            <footer>
            <#switch formData.brand>
                <#case "rbs">
                <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                style="width:650px;color:grey;font-family:Calibri;font-size: 12px">
                    <tr>
                        <hr />
                        <td>
                            <p>The Royal Bank of Scotland plc Registered in Scotland No 83026.
                                Registered
                                Office: 36 St Andrew Square, Edinburgh EH2 2YB. <br /><br />
                                Authorised by the Prudential Regulation Authority and regulated by the
                                Financial Conduct Authority and the Prudential Regulation Authority.
                            </p>
                        </td>
                    </tr>
                </table>
                <#break>
                <#case "ubn">
                <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                style="width:650px;color:grey;font-family:Calibri;font-size: 12px">
                    <tr>
                        <hr />
                        <td>
                            <p>  Ulster Bank, a business name of National Westminster Bank Plc (?NatWest?), registered in England and Wales 
                                (Registered Number 929027). Registered Office: 250 Bishopsgate, London, EC2M 4AA. <br /><br />
                                Authorised by the Prudential Regulation Authority and regulated by the Financial Conduct Authority and the 
                                Prudential Regulation Authority, and entered on the Financial Services Register (Registration Number 121878).</p>
                        </td>
                    </tr>
                </table>
                <#break>
                <#case "ulsterbank">
                <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                style="width:650px;color:grey;font-family:Calibri;font-size: 12px">
                    <tr>
                        <hr />
                        <td>
                            <p>  Ulster Bank, a business name of National Westminster Bank Plc (?NatWest?), registered in England and Wales 
                                (Registered Number 929027). Registered Office: 250 Bishopsgate, London, EC2M 4AA. <br /><br />
                                Authorised by the Prudential Regulation Authority and regulated by the Financial Conduct Authority and the 
                                Prudential Regulation Authority, and entered on the Financial Services Register (Registration Number 121878).</p>
                        </td>
                    </tr>
                </table>
                <#break>
                <#default>
                <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                    style="width:650px;color:grey;font-family:Calibri;font-size: 12px">
                    <tr>
                        <hr />
                        <td>
                            <p>National Westminster Bank Plc. Registered in England and Wales
                                No.929027. Registered Office: 250 Bishopsgate, London EC2M
                                4AA.<br /><br />
                                Authorised by the Prudential Regulation Authority and regulated
                                by the Financial Conduct Authority and the Prudential Regulation
                                Authority.<br /><br />
                            </p>
                        </td>
                    </tr>
                </table> 
            </#switch>
            </footer>
        </center>
    </body>
</html>