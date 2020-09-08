(function() {
    var BillsFactory = function($http) {

        var factory = {};
        factory.reportBills = function() {
            return $http.get('/ws/bills/report/');
        };

        return factory;
    };

    angular.module('FinancesApp').factory('BillsFactory', BillsFactory);

}());