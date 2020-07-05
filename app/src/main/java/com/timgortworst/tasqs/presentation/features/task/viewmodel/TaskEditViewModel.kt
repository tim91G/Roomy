package com.timgortworst.tasqs.presentation.features.task.viewmodel

import androidx.lifecycle.*
import com.timgortworst.tasqs.R
import com.timgortworst.tasqs.domain.model.Task
import com.timgortworst.tasqs.domain.model.response.Response
import com.timgortworst.tasqs.domain.usecase.None
import com.timgortworst.tasqs.domain.usecase.task.CreateOrUpdateTaskUseCaseImpl
import com.timgortworst.tasqs.presentation.base.model.Event
import com.timgortworst.tasqs.presentation.usecase.task.CreateOrUpdateTaskUseCase
import com.timgortworst.tasqs.presentation.usecase.user.GetTaskUsersUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TaskEditViewModel(
    private val createOrUpdateTaskUseCase: CreateOrUpdateTaskUseCase,
    getTaskUsersUseCase: GetTaskUsersUseCase
) : ViewModel() {
    val taskUsersLiveData = getTaskUsersUseCase.execute(None).asLiveData(viewModelScope.coroutineContext)

    private val _actionDone = MutableLiveData<Event<Response<Task>>>()
    val actionDone: LiveData<Event<Response<Task>>>
        get() = _actionDone

    fun taskDoneClicked(task: Task) = viewModelScope.launch {
        if (task.description.isEmpty()) {
            _actionDone.value = Event(Response.Empty(R.string.task_edit_error_empty_description))
            return@launch
        }
        val params = CreateOrUpdateTaskUseCaseImpl.Params(task)
        createOrUpdateTaskUseCase.execute(params).collect {
            _actionDone.value = Event(it)
        }
    }
}
