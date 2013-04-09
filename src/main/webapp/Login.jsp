<html lang="de-DE" xml:lang="de-DE">
    <head>
        <meta content="no-cache" http-equiv="Cache-Control"/>
        <meta content="no-cache" http-equiv="Pragma"/>
        <title>Timetracker Anmeldung</title>
        <link href="/favicon.ico" rel="shortcut icon"/>
        <link href="Resources/stylesheet.css" rel="stylesheet" type="text/css"/>
    </head>
    <body>
        <%@ include file="Resources/header/headerWithoutButtons.html" %>
        
        <table align="center" class="MainTable" border="0" cellpadding="0" cellspacing="0" summary="" width="100%">
            <tbody>
                <tr style="height: 100%;" valign="top">
                    <td id="main" width="100%">
                        <h1>Timetracker-Anmeldung</h1>
                        <%@include file="/WEB-INF/jspf/HandleErrorMsg.jspf" %>
                        <form method="post" action="CheckPwd">
                            <table class="CenterTable" border="0" cellspacing="0" cellpadding="25">
                                <tr>
                                    <th align="right">Benutzername:</th>
                                    <td align="left"><input type="text" name="username"></td>
                                </tr>
                                <tr>
                                    <th align="right">Passwort:</th>
                                    <td align="left"><input type="password" name="password"></td>
                                </tr>
                                <tr>
                                    <td align="right"><input type="submit" value="Log In"></td>
                                    <td align="left"><input type="reset"></td>
                                </tr>
                            </table>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
        <%@ include file="Resources/header/footerWithoutButtons.html" %>
    </body>
</html>
