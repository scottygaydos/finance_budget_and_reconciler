(function() {
    var BillsController = function($scope, BillsFactory, $log) {

        $scope.billReport = null;
        $scope.error = null;


        function init() {
            $scope.billReport = BillsFactory.reportBills()
                .success(function(billReport) {
                    $scope.billReport = billReport;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });
        }

        init();

    };

    angular.module('FinancesApp').controller('BillsController', BillsController);
}());