if (window.jQuery) {

    $(function() {
        Domo.elqData = {};

        var getElqValueByFieldname = function(fieldname) {
            if (fieldname in Domo.elqData) {
                return Domo.elqData[fieldname];
            }
        }

        var loadField = function(fieldEl, value) {
            if (value && !fieldEl.val()) {
                if (fieldEl.is('input')) {
                    fieldEl.val(value);
                    fieldEl.trigger('blur');
                } else if (fieldEl.is('select')) {
                    if ($("option[value='"+value+"']", fieldEl).length > 0) {
                        fieldEl.val(value);
                    } else {
                        fieldEl.append($('<option>', {
                            value: value,
                            text : value
                        }));
                        fieldEl.val(value);
                    }
                    fieldEl.trigger('change');
                }
            }
        }

        var prePopForm = function() {
            var fields = {
                email: getElqValueByFieldname('email'),
                firstName: getElqValueByFieldname('firstName'),
                lastName: getElqValueByFieldname('lastName'),
                phone: getElqValueByFieldname('phone'),
                company: getElqValueByFieldname('company'),
                department: getElqValueByFieldname('department'),
                title: getElqValueByFieldname('title'),
                city: getElqValueByFieldname('city'),
                state: getElqValueByFieldname('state'),
                country: getElqValueByFieldname('country'),
                revenue: getElqValueByFieldname('revenue'),
                owner: getElqValueByFieldname('owner')
            };

            loadField($('input#email'), fields.email);
            loadField($('input[name=first_name]'), fields.firstName);
            loadField($('input[name=last_name]'), fields.lastName);
            loadField($('input[name=phone]'), fields.phone);
            loadField($('input#company'), fields.company);
            // loadField($('input#city'), fields.city);
            // loadField($('input#state'), fields.state);
            // loadField($('input#revenue'), fields.revenue);
            loadField($('input[name=title]'), fields.title);
            // loadField($('input#owner_id'), fields.owner);
            // loadField($('select#country'), fields.country);
            loadField($('select[name=department]'), fields.department);
        }

        var performVisitorLookup = function(elqGUID) {
            if ($('.master_form_container form').length > 0) {
                $.ajax({
                    method: "POST",
                    url: "/api/visitor",
                    dataType: 'json',
                    data: {
                        elqContactID: Domo.elq_cid,
                        sfdcContactID: Domo.sfdc_cid,
                        elqGUID: elqGUID
                    }
                }).success(function(data) {
                    Domo.elqData = data;
                    prePopForm();
                });
            }
        }

        var performGUIDLookup = function() {
            var elqTimerId = null;
            var elqTimeout = 5;

            window.waitUntilCustomerGUIDIsRetrieved = function() {
                if (elqTimerId) {
                    if (0 == elqTimeout) {
                        return;
                    }

                    if (typeof this.GetElqCustomerGUID === 'function') {
                        var elqGUID = GetElqCustomerGUID();
                        $('.elqCustomerGUID').val(elqGUID);
                        performVisitorLookup(elqGUID);
                        return;
                    }

                    elqTimeout -= 1
                }
                elqTimerId = setTimeout('waitUntilCustomerGUIDIsRetrieved()', 500);
            }

            $(window).on('load', waitUntilCustomerGUIDIsRetrieved);
        }

        performGUIDLookup();
    });
}
