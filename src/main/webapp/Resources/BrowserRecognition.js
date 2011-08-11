var browser = null;

function getBrowser() {
    if (browser == null) {
        var a = document.all;
        var b = document.layers;
        var c = document.getElementById;
        var d = navigator.userAgent.search(/Firefox/);
        var e = navigator.userAgent.search(/Opera/);
        var f = navigator.userAgent.search(/Netscape/);

        browser = "";
        if ( (a) && (c) && (e == -1) ) {
            browser = 'IE_ab_5';
        } else if ( (a) && !(c) && (e == -1) ) {
            browser = 'IE_bis_4';
        } else if (d != -1) {
            browser = 'FF';
        } else if (b) {
            browser = 'NS_bis_4';
        } else if ( !(b) && (c) ) {
            browser = 'NS_ab_6';
        } else if (e != -1) {
            browser = 'OP';
        }
    }

    return browser;
}

function isIE() {
    return ( getBrowser().indexOf("IE") != -1 );
}
