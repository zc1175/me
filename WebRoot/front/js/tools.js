Request = {
	QueryString : function(paramName) {
		var reg = new RegExp("[\?&]" + paramName + "=([^&]*)[&]?", "i");
		var paramVal = window.location.search.match(reg);
		return paramVal == null ? "" : paramVal[1];
	}
}

function setCookie(name,value){
    var Days = 365; 
    var exp  = new Date();  
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
function getCookie(name) {
    var arr = document.cookie.match(new RegExp("(^| )"+name+"=([^;]*)(;|$)"));
    if(arr != null) return unescape(arr[2]); return null;
}