package com.nursena.fenlab_android.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun AuthScreen(
    onNavigateHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isLoginTab by remember { mutableStateOf(true) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.Navigate) onNavigateHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(0.0f to Color(0xFF04080F), 0.4f to Color(0xFF071428), 1.0f to Color(0xFF041410))
        ))
        // Dekoratif daireler
        Box(modifier = Modifier.size(280.dp).offset(x = (-70).dp, y = (-50).dp)
            .background(Brush.radialGradient(listOf(Teal400.copy(alpha = 0.14f), Color.Transparent)),
                androidx.compose.foundation.shape.CircleShape))
        Box(modifier = Modifier.size(240.dp).align(Alignment.BottomEnd).offset(x = 50.dp, y = 50.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF004D40).copy(alpha = 0.25f), Color.Transparent)),
                androidx.compose.foundation.shape.CircleShape))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            LogoSection()
            Spacer(Modifier.height(14.dp))
            TabSelector(isLogin = isLoginTab, onLogin = { isLoginTab = true; viewModel.clearError() }, onSignup = { isLoginTab = false; viewModel.clearError() })
            Spacer(Modifier.height(6.dp))

            AnimatedVisibility(visible = isLoginTab, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                LoginForm(uiState = uiState, viewModel = viewModel)
            }
            AnimatedVisibility(visible = !isLoginTab, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                RegisterForm(uiState = uiState, viewModel = viewModel)
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isLoginTab) "Hesabın yok mu? " else "Zaten hesabın var mı? ", color = TextSecondary, fontSize = 11.sp)
                Text(
                    if (isLoginTab) "Kayıt Ol" else "Giriş Yap",
                    color = Teal400, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { isLoginTab = !isLoginTab; viewModel.clearError() }
                )
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(44.dp).background(
                Brush.linearGradient(listOf(Teal400, Color(0xFF00A896))),
                androidx.compose.foundation.shape.CircleShape
            ),
            contentAlignment = Alignment.Center
        ) { Text("⚗️", fontSize = 18.sp) }
        Spacer(Modifier.height(12.dp))
        Row {
            Text("Fen", color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text("lab", color = Teal400, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(4.dp))
        Text("Bilimi keşfetmeye başla", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TabSelector(isLogin: Boolean, onLogin: () -> Unit, onSignup: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF0E1E35)).padding(3.dp)) {
        Row {
            TabItem("Giriş Yap", isLogin, onLogin, Modifier.weight(1f))
            TabItem("Kayıt Ol", !isLogin, onSignup, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(9.dp))
            .background(if (selected) Brush.linearGradient(listOf(Teal400, Color(0xFF00A896))) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
            .clickable(onClick = onClick).padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) DarkBg else TextSecondary, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun LoginForm(uiState: AuthUiState, viewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current
    Column {
        AuthTextField(value = uiState.loginUsernameOrEmail, onValueChange = viewModel::onLoginUsernameChange,
            label = "Kullanıcı adı veya e-posta",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
            imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
        Spacer(Modifier.height(6.dp))
        PasswordTextField(value = uiState.loginPassword, onValueChange = viewModel::onLoginPasswordChange, label = "Şifre",
            imeAction = ImeAction.Done, onImeAction = { focusManager.clearFocus(); viewModel.login() })
        ErrorText(error = uiState.error)
        Spacer(Modifier.height(9.dp))
        AuthButton(text = "Giriş Yap", isLoading = uiState.isLoading, onClick = viewModel::login)
    }
}

@Composable
private fun RegisterForm(uiState: AuthUiState, viewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current
    Column {
        AuthTextField(uiState.registerFullName, viewModel::onRegisterFullNameChange, "Ad Soyad",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
            imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
        Spacer(Modifier.height(6.dp))
        AuthTextField(uiState.registerUsername, viewModel::onRegisterUsernameChange, "Kullanıcı adı",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
            imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
        Spacer(Modifier.height(6.dp))
        AuthTextField(uiState.registerEmail, viewModel::onRegisterEmailChange, "E-posta",
            leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
        Spacer(Modifier.height(6.dp))
        PasswordTextField(uiState.registerPassword, viewModel::onRegisterPasswordChange, "Şifre (en az 6 karakter)",
            imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
        Spacer(Modifier.height(7.dp))
        Text("Hesap Türü", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        RoleSelector(selected = uiState.registerRole, onSelect = viewModel::onRegisterRoleChange)
        AnimatedVisibility(visible = uiState.registerRole == UserRole.TEACHER) {
            Column {
                Spacer(Modifier.height(6.dp))
                AuthTextField(uiState.registerBranch, viewModel::onRegisterBranchChange, "Branş",
                    leadingIcon = { Icon(Icons.Default.School, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                    imeAction = ImeAction.Next, onImeAction = { focusManager.moveFocus(FocusDirection.Down) })
                Spacer(Modifier.height(6.dp))
                AuthTextField(uiState.registerExperienceYears, viewModel::onRegisterExperienceYearsChange, "Deneyim yılı",
                    leadingIcon = { Icon(Icons.Default.DateRange, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onImeAction = { focusManager.clearFocus() })
            }
        }
        ErrorText(error = uiState.error)
        Spacer(Modifier.height(9.dp))
        AuthButton(text = "Kayıt Ol", isLoading = uiState.isLoading, onClick = viewModel::register)
    }
}

@Composable
private fun RoleSelector(selected: UserRole, onSelect: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        listOf(UserRole.USER to "Öğrenci", UserRole.TEACHER to "Öğretmen").forEach { (role, label) ->
            val isSelected = selected == role
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) Teal400.copy(alpha = 0.12f) else Color(0xFF0E1E35))
                    .border(1.dp, if (isSelected) Teal400 else DarkSurface3, RoundedCornerShape(9.dp))
                    .clickable { onSelect(role) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isSelected) Teal400 else TextSecondary, fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color(0xFF4A5A75), fontSize = 12.sp) },
        leadingIcon = leadingIcon, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
        shape = RoundedCornerShape(11.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0E1E35), unfocusedContainerColor = Color(0xFF0E1E35),
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            cursorColor = Teal400, focusedIndicatorColor = Teal400,
            unfocusedIndicatorColor = Color.Transparent, focusedLeadingIconColor = Teal400
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    imeAction: ImeAction = ImeAction.Done, onImeAction: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color(0xFF4A5A75), fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null,
                    tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }),
        shape = RoundedCornerShape(11.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF0E1E35), unfocusedContainerColor = Color(0xFF0E1E35),
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            cursorColor = Teal400, focusedIndicatorColor = Teal400,
            unfocusedIndicatorColor = Color.Transparent, focusedLeadingIconColor = Teal400
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                if (!isLoading) Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))
                else Brush.linearGradient(listOf(DarkSurface3, DarkSurface3)),
                RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) CircularProgressIndicator(color = Teal400, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(text, color = DarkBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorText(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Text(error ?: "", color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
}