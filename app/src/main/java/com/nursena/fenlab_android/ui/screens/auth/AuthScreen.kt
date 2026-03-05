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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

        // ── Tam ekran koyu gradient arka plan ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF04080F),
                        0.4f to Color(0xFF071428),
                        1.0f to Color(0xFF041410)
                    )
                )
        )

        // ── Teal dekoratif daireler ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Teal400.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    androidx.compose.foundation.shape.CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF004D40).copy(alpha = 0.3f), Color.Transparent)
                    ),
                    androidx.compose.foundation.shape.CircleShape
                )
        )

        // ── İçerik ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            LogoSection()

            Spacer(Modifier.height(48.dp))

            // Tab seçici
            TabSelector(
                isLogin  = isLoginTab,
                onLogin  = { isLoginTab = true; viewModel.clearError() },
                onSignup = { isLoginTab = false; viewModel.clearError() }
            )

            Spacer(Modifier.height(28.dp))

            // Form
            AnimatedVisibility(
                visible = isLoginTab,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                LoginForm(
                    uiState   = uiState,
                    viewModel = viewModel
                )
            }

            AnimatedVisibility(
                visible = !isLoginTab,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                RegisterForm(
                    uiState   = uiState,
                    viewModel = viewModel
                )
            }

            Spacer(Modifier.height(24.dp))

            // Alt geçiş metni
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = if (isLoginTab) "Hesabın yok mu? " else "Zaten hesabın var mı? ",
                    color    = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text      = if (isLoginTab) "Kayıt Ol" else "Giriş Yap",
                    color     = Teal400,
                    fontSize  = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier  = Modifier.clickable {
                        isLoginTab = !isLoginTab
                        viewModel.clearError()
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logo
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Ikon çember
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.linearGradient(listOf(Teal400, Color(0xFF00A896))),
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("⚗️", fontSize = 32.sp)
        }

        Spacer(Modifier.height(14.dp))

        Row {
            Text(
                text       = "Fen",
                color      = TextPrimary,
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text       = "lab",
                color      = Teal400,
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text      = "Bilimi keşfetmeye başla",
            color     = TextSecondary,
            fontSize  = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab seçici
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TabSelector(isLogin: Boolean, onLogin: () -> Unit, onSignup: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E1E35))
            .padding(4.dp)
    ) {
        Row {
            TabItem(label = "Giriş Yap", selected = isLogin,  onClick = onLogin,  modifier = Modifier.weight(1f))
            TabItem(label = "Kayıt Ol",  selected = !isLogin, onClick = onSignup, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected)
                    Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))
                else
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = if (selected) DarkBg else TextSecondary,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Login Formu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginForm(uiState: AuthUiState, viewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current

    Column {
        AuthTextField(
            value       = uiState.loginUsernameOrEmail,
            onValueChange = viewModel::onLoginUsernameChange,
            label       = "Kullanıcı adı veya e-posta",
            leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            imeAction   = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(Modifier.height(12.dp))

        PasswordTextField(
            value         = uiState.loginPassword,
            onValueChange = viewModel::onLoginPasswordChange,
            label         = "Şifre",
            imeAction     = ImeAction.Done,
            onImeAction   = { focusManager.clearFocus(); viewModel.login() }
        )

        // Hata
        ErrorText(error = uiState.error)

        Spacer(Modifier.height(20.dp))

        AuthButton(
            text      = "Giriş Yap",
            isLoading = uiState.isLoading,
            onClick   = viewModel::login
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Register Formu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RegisterForm(uiState: AuthUiState, viewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current

    Column {
        AuthTextField(
            value         = uiState.registerFullName,
            onValueChange = viewModel::onRegisterFullNameChange,
            label         = "Ad Soyad",
            leadingIcon   = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            imeAction     = ImeAction.Next,
            onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value         = uiState.registerUsername,
            onValueChange = viewModel::onRegisterUsernameChange,
            label         = "Kullanıcı adı",
            leadingIcon   = { Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            imeAction     = ImeAction.Next,
            onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value         = uiState.registerEmail,
            onValueChange = viewModel::onRegisterEmailChange,
            label         = "E-posta",
            leadingIcon   = { Icon(Icons.Default.Email, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            keyboardType  = KeyboardType.Email,
            imeAction     = ImeAction.Next,
            onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(Modifier.height(12.dp))

        PasswordTextField(
            value         = uiState.registerPassword,
            onValueChange = viewModel::onRegisterPasswordChange,
            label         = "Şifre (en az 6 karakter)",
            imeAction     = ImeAction.Next,
            onImeAction   = { focusManager.moveFocus(FocusDirection.Down) }
        )
        Spacer(Modifier.height(16.dp))

        // Rol seçici
        Text("Hesap Türü", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        RoleSelector(
            selected = uiState.registerRole,
            onSelect = viewModel::onRegisterRoleChange
        )

        // Branş (sadece öğretmen)
        AnimatedVisibility(visible = uiState.registerRole == UserRole.TEACHER) {
            Column {
                Spacer(Modifier.height(12.dp))
                AuthTextField(
                    value         = uiState.registerBranch,
                    onValueChange = viewModel::onRegisterBranchChange,
                    label         = "Branş (Fen Bilimleri, Fizik...)",
                    leadingIcon   = { Icon(Icons.Default.School, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                    imeAction     = ImeAction.Done,
                    onImeAction   = { focusManager.clearFocus() }
                )
            }
        }

        ErrorText(error = uiState.error)

        Spacer(Modifier.height(20.dp))

        AuthButton(
            text      = "Kayıt Ol",
            isLoading = uiState.isLoading,
            onClick   = viewModel::register
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Rol seçici
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RoleSelector(selected: UserRole, onSelect: (UserRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(UserRole.USER to "Öğrenci", UserRole.TEACHER to "Öğretmen").forEach { (role, label) ->
            val isSelected = selected == role
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Teal400.copy(alpha = 0.15f)
                        else Color(0xFF0E1E35)
                    )
                    .border(
                        1.dp,
                        if (isSelected) Teal400 else DarkSurface3,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(role) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    color      = if (isSelected) Teal400 else TextSecondary,
                    fontSize   = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Paylaşımlı bileşenler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    TextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(label, color = Color(0xFF4A5A75), fontSize = 14.sp) },
        leadingIcon   = leadingIcon,
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        shape  = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = Color(0xFF0E1E35),
            unfocusedContainerColor = Color(0xFF0E1E35),
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = Teal400,
            focusedIndicatorColor   = Teal400,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = Teal400
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    TextField(
        value               = value,
        onValueChange       = onValueChange,
        placeholder         = { Text(label, color = Color(0xFF4A5A75), fontSize = 14.sp) },
        leadingIcon         = { Icon(Icons.Default.Lock, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
        trailingIcon        = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine           = true,
        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions      = KeyboardActions(onDone = { onImeAction() }),
        shape  = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = Color(0xFF0E1E35),
            unfocusedContainerColor = Color(0xFF0E1E35),
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = Teal400,
            focusedIndicatorColor   = Teal400,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = Teal400
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        enabled  = !isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (!isLoading)
                        Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))
                    else
                        Brush.linearGradient(listOf(DarkSurface3, DarkSurface3)),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color     = Teal400,
                    modifier  = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text       = text,
                    color      = DarkBg,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorText(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Text(
            text     = error ?: "",
            color    = Color(0xFFFF6B6B),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}