(function() {
    var LoginController = function($scope, LoginService, $location) {

        $scope.submit = function() {
            $scope.loginResult = LoginService.getLoginResponse($scope.personName, $scope.password)
                .then(
                    function(response) {
                        if (response.data.resultCode == 100) {
                            $location.path('/transactions/');
                        } else {
                            alert('Login failed: '.concat(response.data.message));
                        }
                    }, function (loginResult, status, headers, config) {
                        $scope.error = "Could not get data";
                    }
                );
        }
    };

    angular.module('FinancesApp').controller('LoginController', LoginController);
}());