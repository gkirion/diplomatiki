var DoPlugins;!function(DoPlugins){function timeParting(s){return s.eVar2=s.getTimeParting("h","-7"),s.eVar3=s.getTimeParting("d","-7"),s}function concatGoogleParams(s){return[s.Util.getQueryParam("gcreative"),s.Util.getQueryParam("gdevice"),s.Util.getQueryParam("gnetwork"),s.Util.getQueryParam("gkeyword"),s.Util.getQueryParam("gplacement"),s.Util.getQueryParam("gmatchtype"),s.Util.getQueryParam("gtarget"),s.Util.getQueryParam("gadposition")].join(":")}function parseGoogleSearchParams(s){var referrer=s.referrer;"https://www.google.com/"===document.referrer&&(referrer="https://www.google.com/?q=google%20secure%20search");var documentReferrer=document.referrer,googleSearchQuery=s.Util.getQueryParam("q","",documentReferrer),ks=s.getQueryParam("esrc","",documentReferrer);if(documentReferrer.indexOf("www.google.com")&&!googleSearchQuery&&"s"===ks){var googleQueryParams=documentReferrer.split("q="),googleSecureSearchString="q=Google%20Secure%20Search";referrer=googleQueryParams[0]+googleSecureSearchString+googleQueryParams[1]}return referrer}function createCampaignString(utmSource,utmMedium,utmCampaign,campId,gCreative,gNetwork,gKeyword){return"utm|source="+utmSource+"|medium="+utmMedium+"|campaign="+utmCampaign+"|campid="+campId+"|gcreative="+gCreative+"|gnetwork="+gNetwork+"|gkeyword="+gKeyword}function setChannelVars(s){return s.channelManager("dkw","",0,1),s.eVar7=s._campaign,s.eVar8=s._keywords,s.getQueryParam("gcreative")?s.eVar7="go"+s.getQueryParam("gcreative"):s.getQueryParam("aid")&&(s.eVar7=s.getQueryParam("aid")),document.location.search.match("utm_")&&(s.eVar7=createCampaignString(s.getQueryParam("utm_source"),s.getQueryParam("utm_medium"),s.getQueryParam("utm_campaign"),s.getQueryParam("campid"),s.getQueryParam("gcreative"),s.getQueryParam("gnetwork"),s.getQueryParam("gkeyword"))),s.eVar7&&(s.eVar10="D=v7"),s.eVar8&&(s.eVar11="D=v8"),""!==s.getQueryParam("rid")&&(s.eVar35=s.getQueryParam("rid"),s.eVar64="D=v35"),s}function doPlugins(){var s=window.s;"function"==typeof s.Util.getQueryParam&&s.Util.getQueryParam("pid")&&"string"==typeof s.pageName&&"+"!==s.pageName.slice(-1)&&(s.pageName=s.pageName+"+"+s.Util.getQueryParam("pid").replace(/[^a-z0-9\-]/gi,"")+"+"),s=timeParting(s);var today=new Date;s.prop3=today.getUTCDate()+"-"+today.getUTCMonth()+"-"+today.getUTCFullYear()+" "+today.getUTCHours()+":"+today.getUTCMinutes()+":"+today.getUTCSeconds(),s.prop5="undefined"!=typeof window.Visitor&&s.visitor?s.visitor.getMarketingCloudVisitorID():"VisitorAPI Missing",s.eVar24="D=c5",s.eVar48=s.getVisitNum(),s.eVar68=concatGoogleParams(s),s.referrer=parseGoogleSearchParams(s),s=setChannelVars(s),"string"==typeof s.eVar10&&"lnkd.in"===s.eVar10.toLowerCase()&&(s.eVar10=""),"string"==typeof s.eVar7&&"lnkd.in"===s.eVar7.toLowerCase()&&(s.eVar7=""),s.campaign=s.getQueryParam("campid")||s.getQueryParam("gcreative")||s.getQueryParam("aid")||s.getQueryParam("dkw"),document.location.search.match("utm_")&&(s.campaign=createCampaignString(s.getQueryParam("utm_source"),s.getQueryParam("utm_medium"),s.getQueryParam("utm_campaign"),s.getQueryParam("campid"),s.getQueryParam("gcreative"),s.getQueryParam("gnetwork"),s.getQueryParam("gkeyword"))),s.eVar1="D=pageName",s.prop9="D=g",s.eVar20="D=g",s.eVar30="D=pageName",s.eVar43="D=g",s.prop50=20180215,"object"==typeof window.Domo&&"undefined"!=typeof window.Domo.tier&&(s.eVar76=window.Domo.tier),s.eVar49&&(s.prop48="D=v49"),s.prop6=window.location.hostname,window.performance&&window.performance.timing&&window.performance.timing.domContentLoadedEventEnd&&(s.prop10=Math.floor((window.performance.timing.domContentLoadedEventEnd-window.performance.timing.navigationStart)/1e3),0===s.prop10&&(s.prop10="< 1")),!document.cookie.match(/\bnotice_gdpr_prefs=[^:]*1[^:]*:/)&&document.cookie.match("notice_gdpr_prefs=")&&(s.abort=!0)}DoPlugins.doPlugins=doPlugins}(DoPlugins||(DoPlugins={})),function(){"use strict";function getCookie(name){var match=document.cookie.match(new RegExp("(^| )"+name+"=([^;]+)"));if(match)return match[2]}function DomoAnalytics(s){this.s=s,this.videoCompletionEvents={0:{event:"event13",isComplete:!1},.5:{event:"event14",isComplete:!1},1:{event:"event15",isComplete:!1}};var gdprCookie=getCookie("notice_gdpr_prefs");if("string"==typeof gdprCookie){var gdprCookieSplit=void 0;"string"==typeof gdprCookie&&(gdprCookieSplit=gdprCookie.split(","))}}DomoAnalytics.prototype.trackVimeo=function($fVideo,vimeoID){$fVideo.addEvent("playProgress",function(event){var percent=event.percent;this.trackVideoEvents(percent,"Vimeo: "+vimeoID)}.bind(this))},DomoAnalytics.prototype.trackTNT=function(){window.s_tnt=window.s_tnt||"";var tntVal="${campaign.id}:${campaign.recipe.id}:${campaign.recipe.trafficType},";if(window.s_tnt.indexOf(tntVal)===-1&&(window.s_tnt+=tntVal),window.mboxFactories.get("default").isDomLoaded()&&window.s&&window.s.tl){var ltv=this.s.linkTrackVars,lte=this.s.linkTrackEvents;this.s.linkTrackVars="tnt",this.s.linkTrackEvents="None",this.s.tl("TnT","o","TnT"),this.s.linkTrackVars=ltv,this.s.linkTrackEvents=lte}},DomoAnalytics.prototype.trackVideoEvents=function(status,title){Object.keys(this.videoCompletionEvents).forEach(function(time){!this.videoCompletionEvents[time].isComplete&&status>=parseFloat(time)&&(this.videoCompletionEvents[time].isComplete=!0,this.s.clearVars(),this.s.eVar22=title,this.s.events=this.videoCompletionEvents[time].event,this.s.tl(!0,"o","videoEvent"))}.bind(this))},DomoAnalytics.prototype.trackFormSubmit=function(form){this.s.clearVars();var trackedFields=["company","company_size","department"];this.s.list3=[];for(var i=0;i<form.getElementsByTagName("input").length;i++)trackedFields.indexOf(form[i].name)!==-1&&this.s.list3.push(form[i].value);this.s.tl(!0,"o","trackFormSubmit")},DomoAnalytics.prototype.doPlugins=DoPlugins.doPlugins,window.domoAnalytics=new DomoAnalytics(window.s)}();