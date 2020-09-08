(function() {
    var ReconcileItemFactory = function($http) {

        var factory = {};
        factory.reportReconcileItems = function() {
            return $http.get('/ws/reconcile/report/');
        };

        return factory;
    };

    angular.module('FinancesApp').factory('ReconcileItemFactory', ReconcileItemFactory);

}());