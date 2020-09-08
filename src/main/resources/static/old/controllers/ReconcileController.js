(function() {
    var ReconcileController = function($scope, ReconcileItemFactory) {

        $scope.mintTransactionsToReconcile = null;
        $scope.error = null;


        function init() {
            $scope.mintTransactionsToReconcile = ReconcileItemFactory.reportReconcileItems()
                .success(function(mintTransactionsToReconcile) {
                    $scope.mintTransactionsToReconcile = mintTransactionsToReconcile;
                })
                .error(function (data, status, headers, config) {
                    $scope.error = "Could not get data";
                });

        }

        init();

    };

    angular.module('FinancesApp').controller('ReconcileController', ReconcileController);
}());