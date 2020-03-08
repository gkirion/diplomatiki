(function () {
    var hideBanner = function(reload) {
        var banner = document.getElementById('cookie-consent-banner');
        if (banner) {
            banner.className = 'hide';
        }

        if (reload) {
            window.location.reload();
        }
    };

    window.Domo.gdpr = {
        acceptAll: function (event) {
            if (event) {
                event.preventDefault();
            }

            hideBanner(event);

            this.setTrackingLevel('0,1,2');
        },

        acceptMarketing: function (event) {
            if (event) {
                event.preventDefault();
            }

            hideBanner(event);

            this.setTrackingLevel('0,1');
        },

        reject: function (event) {
            if (event) {
                event.preventDefault();
            }

            hideBanner(event);

            this.setTrackingLevel('0');
        },

        setTrackingLevel: function(level) {
            var date = new Date();
            date.setTime(date.getTime()+(90*24*60*60*1000));

            document.cookie = 'notice_gdpr_prefs='+ level + ':;domain=' + window.location.host + ';expires=' + date.toGMTString() + ';path=/';
        },

        initBanner: function() {
          var htmlNode = document.createElement('div');
          htmlNode.id = "cookie-consent-banner";
          htmlNode.innerHTML = '' +
            '<div id="inner-cookie-banner" style="position: relative; max-width: 1045px; padding: 10px">' +
            '<div style="max-width: 950px; padding: 0px;">' +
            window.trans('cookieconsent.copy') +
            '</div>' +
            '<div style="margin: auto 0">' +
            '<span class="cookie-learn-more">' +
            '<a href="/company/cookies" onclick="window.Domo.gdpr.trackBanner()">' + window.trans('cookieconsent.learn') + '</a>' +
            '</span>' +
            '</div>' +
            '<img class="close" src="https://web-assets.domo.com/miyagi/images/icon/domo-icon/domo-icon-delete-exit-remove-close-dark.svg" onclick="window.Domo.gdpr.closeBanner()">';

          document.body.insertBefore(htmlNode, null);

          var styleNode = document.createElement('link');
          styleNode.setAttribute('rel', 'stylesheet');
          styleNode.setAttribute('href', '/assets/css/consent-banner.css');
          document.getElementsByTagName("head")[0].appendChild(styleNode);

          if (window.s) {
            window.s.clearVars();
            window.s.events = 'event52';
            window.s.tl(true, 'o', 'gdpr_fire');
          }

          window.Domo.gdpr.setTrackingLevel('0,1,2')
        }
    };

    document.addEventListener('DOMContentLoaded', function() {
        window.Domo.gdpr.closeBanner = function() {
            window.Domo.gdpr.acceptAll();
            if(window.$) {
              window.$.publish('analytics.trackLink', {name: 'close', position: 'gdpr-banner'});
            }
        };

        window.Domo.gdpr.trackBanner = function() {
            if(window.$) {
              window.$.publish('analytics.trackLink', {name: 'cookies', position: 'gdpr-banner'});
            }
        };

        if (window.Domo && window.Domo.isGDPR && window.Domo.gdpr && !document.cookie.match(/\bnotice_gdpr_prefs=/) ) {
            window.Domo.gdpr.initBanner();
        }
    });
})();
