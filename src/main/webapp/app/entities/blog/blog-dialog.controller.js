(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('BlogDialogController', BlogDialogController);

    BlogDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Blog', 'User', 'Posts'];

    function BlogDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Blog, User, Posts) {
        var vm = this;

        vm.blog = entity;
        vm.clear = clear;
        vm.save = save;
        vm.users = User.query();
        vm.posts = Posts.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.blog.id !== null) {
                Blog.update(vm.blog, onSaveSuccess, onSaveError);
            } else {
                Blog.save(vm.blog, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('blogisterApp:blogUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
