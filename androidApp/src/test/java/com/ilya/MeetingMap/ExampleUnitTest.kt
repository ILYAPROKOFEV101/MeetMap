package com.ilya.MeetingMap

import org.junit.Test

import org.junit.Assert.*


import android.content.Context
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Verify.verify
import com.ilya.MeetingMap.SocialMap.ViewModel.VIewModelUser

import com.ilya.codewithfriends.presentation.profile.IMG
import com.ilya.codewithfriends.presentation.profile.UID
import com.ilya.codewithfriends.presentation.profile.ID
import com.ilya.codewithfriends.presentation.sign_in.GoogleAuthUiClient
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify



import com.ilya.codewithfriends.presentation.profile.IMG
import com.ilya.codewithfriends.presentation.profile.UID
import com.ilya.codewithfriends.presentation.profile.ID

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before

data class UserData(
    val id: String,
    val photoUrl: String,
    val name: String
)


@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelUserTest {

    private val context: Context = mockk()
    private val googleAuthUiClient: GoogleAuthUiClient = mockk()
    private lateinit var viewModel: VIewModelUser

    @Before
    fun setup() {
        // Инициализация тестового диспетчера
        Dispatchers.setMain(StandardTestDispatcher())

        // Мок GoogleAuthUiClient
        every { googleAuthUiClient.getSignedInUser() } returns mockk {
            every { id } returns "123"
            every { photoUrl } returns "http://example.com/image.jpg"
            every { name } returns "Test User"
        }

        // Создание ViewModel с замоком
        viewModel = spyk(VIewModelUser(context), recordPrivateCalls = true)
        every { viewModel["googleAuthUiClient"] } returns googleAuthUiClient
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test Get_UID returns correct UID`() = runTest {
        val uid = viewModel.Get_UID(context)
        assertThat(uid).isEqualTo("123")
        verify { googleAuthUiClient.getSignedInUser() }
    }

    @Test
    fun `test Get_IMG returns correct image URL`() = runTest {
        val img = viewModel.Get_IMG(context)
        assertThat(img).isEqualTo("http://example.com/image.jpg")
        verify { googleAuthUiClient.getSignedInUser() }
    }

    @Test
    fun `test Get_NAME returns correct name`() = runTest {
        val name = viewModel.Get_NAME(context)
        assertThat(name).isEqualTo("Test User")
        verify { googleAuthUiClient.getSignedInUser() }
    }

    @Test
    fun `test Get_Key handles null and sends request`() = runTest {
        mockkStatic("com.ilya.codewithfriends.presentation.profile.ProfileUtilsKt") // Для моков ID()
        val client = mockk<OkHttpClient>()
        every { viewModel["getUserKey"](context) } returns null andThen "generated_key"
        coEvery { viewModel["sendGetRequest"](any(), client, context) } just Runs

        val key = viewModel.Get_Key(context)
        assertThat(key).isEqualTo("generated_key")
        coVerify { viewModel["sendGetRequest"]("123", client, context) }
    }
}
