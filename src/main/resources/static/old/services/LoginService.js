(function() {
    var LoginService = function($http) {

        var service = {};
        service.getLoginResponse = function(personName, password) {

            //return authentication object;
            /*return $http({
                url: "/ws/authentication/",
                method: "POST",
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                data: $.param({personName: personName, password: password})
            });*/


            return $http.post(
                '/ws/authentication/',
                $.param({personName: personName, password: password}),
                {headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}}
            );
        };

        return service;
    };

    angular.module('FinancesApp').factory('LoginService', LoginService);

}());
