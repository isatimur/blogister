(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('TagDetailController', TagDetailController);

    TagDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Tag', 'Posts'];

    function TagDetailController($scope, $rootScope, $stateParams, entity, Tag, Posts) {
        var vm = this;

        vm.tag = entity;

        var unsubscribe = $rootScope.$on('blogisterApp:tagUpdate', function(event, result) {
            vm.tag = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
