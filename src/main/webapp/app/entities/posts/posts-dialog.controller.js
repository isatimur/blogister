(function() {
    'use strict';

    angular
        .module('blogisterApp')
        .controller('PostsDialogController', PostsDialogController);

    PostsDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Posts', 'Tag', 'Blog'];

    function PostsDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Posts, Tag, Blog) {
        var vm = this;

        vm.posts = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.tags = Tag.query();
        vm.blogs = Blog.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.posts.id !== null) {
                Posts.update(vm.posts, onSaveSuccess, onSaveError);
            } else {
                Posts.save(vm.posts, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('blogisterApp:postsUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.creattionDate = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
