(function() {
    var MoneyOwedController = function($scope, MoneyOwedFactory) {

        $scope.moneyOwedList = null;
        $scope.error = null;


        function init() {
            $scope.moneyOwedList = MoneyOwedFactory.reportMoneyOwedTotals()
                .success(function(moneyOwedList) {
                    $scope.moneyOwedList = moneyOwedList;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

        }

        init();

    };

    angular.module('FinancesApp').controller('MoneyOwedController', MoneyOwedController);
}());