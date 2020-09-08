(function() {
    var AddBudgetController = function($scope, BudgetFactory, $location) {

        $scope.submit = function() {
            $scope.createTransactionResult = BudgetFactory.createBudgetForMonth(
                $scope.budgetMonth, $scope.budgetYear)
                .then(
                function(response) {
                    if (response.data.resultCode == 100) {
                        $location.path('/budget/');
                    } else {
                        alert('Create budget failed: '.concat(response.data.message));
                    }
                }, function (loginResult, status, headers, config) {
                    $scope.error = "Could not get data";
                }
            );
        };

        function init() {
            var budgetMonth = $scope.budgetMonth;
            if (budgetMonth == null) {
                budgetMonth = new Date().getMonth()+1;
                $scope.budgetMonth = budgetMonth;
                //$log.log("Initializing budgetMonth="+budgetMonth);
            }

            var budgetYear = $scope.budgetYear;
            if (budgetYear == null) {
                budgetYear = new Date().getFullYear();
                $scope.budgetYear = budgetYear;
                //$log.log("Initializing budgetYear="+budgetYear);
            }
        }

        init();

    };

    angular.module('FinancesApp').controller('AddBudgetController', AddBudgetController);
}());