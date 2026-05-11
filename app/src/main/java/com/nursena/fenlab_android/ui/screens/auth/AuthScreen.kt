package com.nursena.fenlab_android.ui.screens.auth

import android.net.Uri
import com.nursena.fenlab_android.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.theme.*

// ─── Yeni renk sabitleri (ikinci tasarım için) ───────────────────────────────
private val SplashBg        = Color(0xFFFFFFFF)
private val FenGreen    = Color(0xFF0D7D7C)   // ana mor (buton, başlık)
private val FenGreenDk  = Color(0xFF095958)   // koyu mor (Sign up butonu)
private val FenGreenLight1  = Color(0xFFE0F4F4)   // shape arka plan
private val FenGreenLight    = Color(0xFFF0E6FA)   // karakter kart bg

private enum class AuthMode { SPLASH, LOGIN, REGISTER }

@Composable
fun AuthScreen(
    onNavigateHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(AuthMode.SPLASH) }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { if (it is UiEvent.Navigate) onNavigateHome() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Arka plan: düz açık lavanta ───────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize().background(SplashBg))

        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                if (targetState == AuthMode.SPLASH) {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                } else {
                    (slideInVertically(tween(350)) { it / 4 } + fadeIn(tween(300))).togetherWith(
                        slideOutVertically(tween(250)) { -it / 4 } + fadeOut(tween(200))
                    )
                }
            },
            label = "auth_mode"
        ) { currentMode ->
            when (currentMode) {
                AuthMode.SPLASH   -> SplashPage(
                    onLogin    = { viewModel.clearError(); mode = AuthMode.LOGIN },
                    onRegister = { viewModel.clearError(); mode = AuthMode.REGISTER }
                )
                AuthMode.LOGIN    -> LoginPage(
                    uiState    = uiState,
                    viewModel  = viewModel,
                    onBack     = { viewModel.clearError(); mode = AuthMode.SPLASH },
                    onGoSignup = { viewModel.clearError(); viewModel.resetRegister(); mode = AuthMode.REGISTER }
                )
                AuthMode.REGISTER -> RegisterFlow(
                    uiState   = uiState,
                    viewModel = viewModel,
                    context   = context,
                    onBack    = { viewModel.clearError(); mode = AuthMode.SPLASH },
                    onGoLogin = { viewModel.clearError(); mode = AuthMode.LOGIN }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SPLASH — ikinci tasarım stilinde
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashPage(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(48.dp))

        // ── Logo ──────────────────────────────────────────────────────────────
        Row {
            Text(
                text = "Fen",
                color = Color(0xFF0D7D7C),
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Lab",
                color = Color(0xCCF99930),
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── Slogan ────────────────────────────────────────────────────────────
        Text(
            text = "Keşfet, Dene, Paylaş",
            color = Color(0xFF555555),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.weight(1f))

        // ── Orta: Deney görseli ───────────────────────────────────────────────
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.creative_experiment),
            contentDescription = "Bilim deneyi görseli",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .aspectRatio(1f)
        )

        Spacer(Modifier.weight(1f))

        // ── Alt butonlar: alt alta ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FenGreen),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Giriş Yap",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LabOrangeNew),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Hesap Oluştur",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GİRİŞ SAYFASI  (iç sayfalar aynı mor tonla uyumlu hale getirildi)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginPage(
    uiState: AuthUiState, viewModel: AuthViewModel,
    onBack: () -> Unit, onGoSignup: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Geri butonu ───────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(FenGreen).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(28.dp)
        ) {
            Column {
                Text("Giriş Yap", color = FenGreen, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text("Seni bekliyorduk!", color = Color(0xFF888888), fontSize = 13.sp)
                Spacer(Modifier.height(24.dp))

                PurpleField(
                    value = uiState.loginUsernameOrEmail,
                    onValueChange = viewModel::onLoginUsernameChange,
                    placeholder = "Kullanıcı adı veya e-posta",
                    icon = Icons.Default.Person,
                    imeAction = ImeAction.Next,
                    onIme = { focusManager.moveFocus(FocusDirection.Down) }
                )
                Spacer(Modifier.height(12.dp))
                PurplePasswordField(
                    value = uiState.loginPassword,
                    onValueChange = viewModel::onLoginPasswordChange,
                    placeholder = "Şifre",
                    imeAction = ImeAction.Done,
                    onIme = { focusManager.clearFocus(); viewModel.login() }
                )

                AnimatedVisibility(visible = uiState.error != null) {
                    Text(uiState.error ?: "", color = Color(0xFFE53935), fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(Modifier.height(24.dp))
                PurpleButton(text = "Giriş Yap", isLoading = uiState.isLoading, onClick = viewModel::login)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Hesabın yok mu? ", color = Color(0xFF888888), fontSize = 13.sp)
                    Text("Kayıt Ol", color = FenGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onGoSignup))
                }
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// KAYIT AKIŞI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RegisterFlow(
    uiState: AuthUiState, viewModel: AuthViewModel,
    context: android.content.Context,
    onBack: () -> Unit, onGoLogin: () -> Unit
) {
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(context, it) }
    }

    val steps = if (uiState.registerRole == UserRole.TEACHER)
        listOf(RegisterStep.ROLE, RegisterStep.REQUIRED, RegisterStep.BRANCH, RegisterStep.EXPERIENCE, RegisterStep.BIO, RegisterStep.PHOTO)
    else
        listOf(RegisterStep.ROLE, RegisterStep.REQUIRED, RegisterStep.BIO, RegisterStep.PHOTO)

    val currentIndex = steps.indexOf(uiState.registerStep).coerceAtLeast(0)
    val progress = (currentIndex + 1f) / steps.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        // ── Üst bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(FenGreenDark)
                    .clickable(onClick = if (uiState.registerStep == RegisterStep.ROLE) onBack else viewModel::prevStep),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("${currentIndex + 1} / ${steps.size}", color = FenGreen, fontSize = 12.sp,
                fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(10.dp))

        // ── Progress bar ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp)).background(FenGreenDark)
            )
        }

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(28.dp)
        ) {
            AnimatedContent(
                targetState = uiState.registerStep,
                transitionSpec = {
                    (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280))).togetherWith(
                        slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(220))
                    )
                },
                label = "step"
            ) { step ->
                when (step) {
                    RegisterStep.ROLE       -> RoleStep(uiState, viewModel, onGoLogin)
                    RegisterStep.REQUIRED   -> RequiredStep(uiState, viewModel)
                    RegisterStep.BRANCH     -> OptionalStep(
                        title = "Branşın nedir?", subtitle = "",
                        value = uiState.registerBranch, onValueChange = viewModel::onRegisterBranchChange,
                        placeholder = "Fen Bilimleri, Fizik, Kimya...",
                        icon = Icons.Default.School, keyboardType = KeyboardType.Text,
                        error = uiState.error, onNext = viewModel::nextStep, onSkip = viewModel::skipStep
                    )
                    RegisterStep.EXPERIENCE -> OptionalStep(
                        title = "Kaç yıllık deneyimin var?", subtitle = "",
                        value = uiState.registerExperienceYears, onValueChange = viewModel::onRegisterExperienceYearsChange,
                        placeholder = "Örn: 5",
                        icon = Icons.Default.WorkHistory, keyboardType = KeyboardType.Number,
                        error = uiState.error, onNext = viewModel::nextStep, onSkip = viewModel::skipStep
                    )
                    RegisterStep.BIO        -> BioStep(uiState, viewModel)
                    RegisterStep.PHOTO      -> PhotoStep(uiState, viewModel) { photoLauncher.launch("image/*") }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Adım: Rol seç
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RoleStep(uiState: AuthUiState, viewModel: AuthViewModel, onGoLogin: () -> Unit) {
    Column {
        Text("Nasıl kullanacaksın?", color = FenGreenDark, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text("Sana özel deneyim için hesap türünü seç.", color = Color(0xFF888888), fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))

        RoleCard(
            emoji = "🎓", title = "Öğrenci",
            bullets = listOf("Deneyleri keşfet ve favorile", "Öğretmenlere soru sor", "Yorum yap ve değerlendir"),
            selected = uiState.registerRole == UserRole.USER,
            onClick = { viewModel.onRegisterRoleChange(UserRole.USER) }
        )
        Spacer(Modifier.height(10.dp))
        RoleCard(
            emoji = "👨‍🏫", title = "Öğretmen",
            bullets = listOf("Deney ekle ve yönet", "Öğrenci sorularını yanıtla", "Sınıf düzeyine göre içerik"),
            selected = uiState.registerRole == UserRole.TEACHER,
            onClick = { viewModel.onRegisterRoleChange(UserRole.TEACHER) }
        )
        Spacer(Modifier.height(22.dp))
        PurpleButton("Devam Et", false, viewModel::nextStep)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Zaten hesabın var mı? ", color = Color(0xFF888888), fontSize = 13.sp)
            Text("Giriş Yap", color = FenGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onGoLogin))
        }
    }
}

@Composable
private fun RoleCard(emoji: String, title: String, bullets: List<String>, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) FenGreenDark else Color(0xFFDDDDDD)
    val bgColor     = if (selected) Color(0xFFF0FAF4) else Color(0xFFF8F8F8)

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Color(0xFFE0F4F4) else Color.White),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 20.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (selected) FenGreen else Color(0xFF444444),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                bullets.forEach { b ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape)
                            .background(if (selected) FenGreen else Color(0xFFAAAAAA)))
                        Text(b, color = Color(0xFF888888), fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }

            if (selected) Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(FenGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Adım: Zorunlu bilgiler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RequiredStep(uiState: AuthUiState, viewModel: AuthViewModel) {
    val focusManager = LocalFocusManager.current
    Column {
        StepHeader("Hesap Bilgilerin", "Bu alanlar zorunludur.")
        Spacer(Modifier.height(18.dp))
        PurpleField(uiState.registerFullName, viewModel::onRegisterFullNameChange, "Ad Soyad",
            Icons.Default.Person, ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
        Spacer(Modifier.height(10.dp))
        PurpleField(uiState.registerUsername, viewModel::onRegisterUsernameChange, "Kullanıcı adı",
            Icons.Default.AlternateEmail, ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
        Spacer(Modifier.height(10.dp))
        PurpleField(uiState.registerEmail, viewModel::onRegisterEmailChange, "E-posta",
            Icons.Default.Email, ImeAction.Next, KeyboardType.Email) { focusManager.moveFocus(FocusDirection.Down) }
        Spacer(Modifier.height(10.dp))
        PurplePasswordField(uiState.registerPassword, viewModel::onRegisterPasswordChange,
            "Şifre (en az 6 karakter)", ImeAction.Done) { focusManager.clearFocus() }
        AnimatedVisibility(visible = uiState.error != null) {
            Text(uiState.error ?: "", color = Color(0xFFE53935), fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(20.dp))
        PurpleButton("Devam Et", false, viewModel::nextStep)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Opsiyonel adım
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OptionalStep(
    title: String, subtitle: String,
    value: String, onValueChange: (String) -> Unit, placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, keyboardType: KeyboardType,
    error: String?, onNext: () -> Unit, onSkip: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column {
        StepHeader(title, subtitle)
        Spacer(Modifier.height(18.dp))
        PurpleField(value, onValueChange, placeholder, icon, ImeAction.Done, keyboardType) { focusManager.clearFocus() }
        AnimatedVisibility(visible = error != null) {
            Text(error ?: "", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(20.dp))
        PurpleButton("Devam Et", false, onNext)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Şimdi atla →", color = Color(0xFF888888), fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Biyografi
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BioStep(uiState: AuthUiState, viewModel: AuthViewModel) {
    Column {
        StepHeader("Kendini Tanıt", "")
        Spacer(Modifier.height(18.dp))
        TextField(
            value = uiState.registerBio, onValueChange = viewModel::onRegisterBioChange,
            placeholder = { Text("Merhaba! Fen bilimlerine meraklıyım...", color = Color(0xFFBBBBBB), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            maxLines = 6, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8F7F7), unfocusedContainerColor = Color(0xFFE8F7F7),
                focusedTextColor = Color(0xFF222222), unfocusedTextColor = Color(0xFF222222),
                cursorColor = FenGreen, focusedIndicatorColor = FenGreen,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(20.dp))
        PurpleButton("Devam Et", false, viewModel::nextStep)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = viewModel::skipStep, modifier = Modifier.fillMaxWidth()) {
            Text("Şimdi atla →", color = Color(0xFF888888), fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profil fotoğrafı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PhotoStep(uiState: AuthUiState, viewModel: AuthViewModel, onLaunchPicker: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepHeader("Profil Fotoğrafı", "İsteğe bağlı, daha sonra da ekleyebilirsin.")
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape)
                .background(Color(0xFFF0FAF4))
                .border(2.dp, if (uiState.registerPhotoUri != null) FenGreen else Color(0xFFDDDDDD), CircleShape)
                .clickable(onClick = onLaunchPicker),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isUploadingPhoto -> CircularProgressIndicator(
                    color = FenGreen, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                uiState.registerPhotoUri != null -> AsyncImage(
                    model = uiState.registerPhotoUri, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = FenGreen, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(3.dp))
                    Text("Ekle", color = FenGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (uiState.registerPhotoUri != null && !uiState.isUploadingPhoto) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = FenGreen, modifier = Modifier.size(14.dp))
                Text("Fotoğraf yüklendi", color = FenGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Fotoğrafına dokunarak seç", color = Color(0xFF888888), fontSize = 12.sp, textAlign = TextAlign.Center)
        AnimatedVisibility(visible = uiState.error != null) {
            Text(uiState.error ?: "", color = Color(0xFFE53935), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(24.dp))
        PurpleButton(
            text = if (uiState.isLoading) "Hesap Oluşturuluyor..." else "Hesabı Oluştur",
            isLoading = uiState.isLoading || uiState.isUploadingPhoto,
            onClick = viewModel::nextStep
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = viewModel::skipStep, modifier = Modifier.fillMaxWidth()) {
            Text("Fotoğrafsız devam et", color = Color(0xFF888888), fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ortak bileşenler  (mor tema ile uyumlu)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column {
        Text(title, color = FenGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Color(0xFF0D7D7C), fontSize = 13.sp)
        }
    }
}

@Composable
private fun PurpleField(
    value: String, onValueChange: (String) -> Unit, placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    onIme: () -> Unit = {}
) {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFBBBBBB), fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onIme() }, onDone = { onIme() }),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE8F7F7), unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedTextColor = Color(0xFF222222), unfocusedTextColor = Color(0xFF222222),
            cursorColor = FenGreen, focusedIndicatorColor = FenGreen,
            unfocusedIndicatorColor = Color.Transparent, focusedLeadingIconColor = FenGreen
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PurplePasswordField(
    value: String, onValueChange: (String) -> Unit, placeholder: String,
    imeAction: ImeAction = ImeAction.Done, onIme: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFBBBBBB), fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onIme() }),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE8F7F7), unfocusedContainerColor = Color(0xFFF0F0F0),
            focusedTextColor = Color(0xFF222222), unfocusedTextColor = Color(0xFF222222),
            cursorColor = FenGreen, focusedIndicatorColor = FenGreen,
            unfocusedIndicatorColor = Color.Transparent, focusedLeadingIconColor = FenGreen
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PurpleButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FenGreen,
            disabledContainerColor = Color(0xFF80CFCE)
        )
    ) {
        if (isLoading) CircularProgressIndicator(
            color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        else Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}