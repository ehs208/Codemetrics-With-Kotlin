package com.github.ehs208.codemetrics.core.config;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

@State(
    name = "CodeMetricsConfiguration",
    storages = {@Storage("CodeMetricsConfiguration.xml")})
public class MetricsConfiguration implements PersistentStateComponent<MetricsConfiguration> {
  private transient List<Runnable> listeners = new LinkedList<>();

  public void notifyListeners() {
    ImmutableList.copyOf(listeners).forEach(Runnable::run);
  }

  public Disposable addListener(Runnable listener) {
    listeners.add(listener);
    return () -> listeners.remove(listener);
  }

  public static MetricsConfiguration getInstance() {
    return ApplicationManager.getApplication().getService(MetricsConfiguration.class);
  }

  @Nullable
  @Override
  public MetricsConfiguration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull MetricsConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
    validateAndFixState();
  }

  /**
   * Validates and corrects configuration values to prevent runtime errors.
   * Called after loading state from disk and before applying user changes.
   * Uses cascading validation to ensure strict ordering: low < normal < high < extreme
   */
  public boolean validateAndFixState() {
    // Capture original values to detect changes
    int originalHiddenUnder = hiddenUnder;
    int originalLow = complexityLevelLow;
    int originalNormal = complexityLevelNormal;
    int originalHigh = complexityLevelHigh;
    int originalExtreme = complexityLevelExtreme;

    // Ensure non-negative values
    if (hiddenUnder < 0) hiddenUnder = 0;

    // Set safe defaults for invalid values (top-down)
    if (complexityLevelExtreme <= 0) complexityLevelExtreme = 25;
    if (complexityLevelHigh <= 0) complexityLevelHigh = 10;
    if (complexityLevelNormal <= 0) complexityLevelNormal = 5;
    if (complexityLevelLow < 0) complexityLevelLow = 0;

    // Ensure strict ordering with cascading validation (bottom-up)
    // This guarantees: low < normal < high < extreme

    // Start from bottom: ensure low < normal
    if (complexityLevelLow >= complexityLevelNormal) {
      complexityLevelLow = Math.max(0, complexityLevelNormal - 1);
    }

    // Ensure normal < high
    if (complexityLevelNormal >= complexityLevelHigh) {
      complexityLevelNormal = complexityLevelHigh - 1;
      // Re-validate low after normal changed
      if (complexityLevelLow >= complexityLevelNormal) {
        complexityLevelLow = Math.max(0, complexityLevelNormal - 1);
      }
    }

    // Ensure high < extreme
    if (complexityLevelHigh >= complexityLevelExtreme) {
      complexityLevelHigh = complexityLevelExtreme - 1;
      // Re-validate normal and low after high changed
      if (complexityLevelNormal >= complexityLevelHigh) {
        complexityLevelNormal = complexityLevelHigh - 1;
        if (complexityLevelLow >= complexityLevelNormal) {
          complexityLevelLow = Math.max(0, complexityLevelNormal - 1);
        }
      }
    }

    // Final safety check: ensure minimum viable thresholds
    // This prevents degenerate cases like extreme=1, high=0, normal=-1, low=-2
    if (complexityLevelExtreme < 4) {
      complexityLevelExtreme = 4;
      complexityLevelHigh = 3;
      complexityLevelNormal = 2;
      complexityLevelLow = 0;
    } else {
      // Ensure each level is at least 1 less than the one above
      if (complexityLevelHigh >= complexityLevelExtreme) {
        complexityLevelHigh = complexityLevelExtreme - 1;
      }
      if (complexityLevelNormal >= complexityLevelHigh) {
        complexityLevelNormal = complexityLevelHigh - 1;
      }
      if (complexityLevelLow >= complexityLevelNormal) {
        complexityLevelLow = Math.max(0, complexityLevelNormal - 1);
      }
    }

    // Return true if any value was modified
    return hiddenUnder != originalHiddenUnder
        || complexityLevelLow != originalLow
        || complexityLevelNormal != originalNormal
        || complexityLevelHigh != originalHigh
        || complexityLevelExtreme != originalExtreme;
  }

  public Integer complexityColorLow = 0xFF4bb14f;
  public Integer complexityColorNormal = 0xFFffc208;
  public Integer complexityColorHigh = 0xFFf44034;
  public Integer complexityColorExtreme = 0xFFff0000;

  public boolean metricsForAnonymousClass = true;
  public boolean metricsForAClass = true;
  public boolean metricsForMethod = true;
  public boolean metricsForLambdaExpression = false;

  public int complexityLevelExtreme = 25;
  public int complexityLevelHigh = 10;
  public int complexityLevelNormal = 5;
  public int complexityLevelLow = 0;

  public int hiddenUnder = 3;

  public String complexityLevelExtremeDescription = "Bloody hell...";
  public String complexityLevelHighDescription = "You must be kidding";
  public String complexityLevelNormalDescription = "It's time to do something...";
  public String complexityLevelLowDescription = "Everything is cool!";
  public String complexityTemplate = "Complexity is {0} {1}";

  public int anonymousClass = 1;
  public String anonymousClassDescription = "Anonymous Class";
  public int arrayAccessExpression = 0;
  public String arrayAccessExpressionDescription = "Array Access Expression";
  public int arrayInitializerExpression = 0;
  public String arrayInitializerExpressionDescription = "Array Initializer Expression";
  public int assertStatement = 0;
  public String assertStatementDescription = "Assert Statement";
  public int assignmentExpression = 0;
  public String assignmentExpressionDescription = "Assignment Expression";
  public int binaryExpression = 1;
  public String binaryExpressionDescription = "Binary Expression";
  public int blockStatement = 0;
  public String blockStatementDescription = "Block Statement";
  public int breakStatement = 1;
  public String breakStatementDescription = "Break Statement";
  public int aClass = 1;
  public String aClassDescription = "Class";
  public int classInitializer = 0;
  public String classInitializerDescription = "Class Initializer";
  public int classObjectAccessExpression = 0;
  public String classObjectAccessExpressionDescription = "Class Object Access Expression";
  public int codeBlock = 0;
  public String codeBlockDescription = "Code Block";
  public int conditionalExpression = 1;
  public String conditionalExpressionDescription = "Conditional Expression";
  public int continueStatement = 1;
  public String continueStatementDescription = "Continue Statement";
  public int declarationStatement = 0;
  public String declarationStatementDescription = "Declaration Statement";
  public int docComment = 0;
  public String docCommentDescription = "Doc Comment";
  public int docTag = 0;
  public String docTagDescription = "Doc Tag";
  public int doWhileStatement = 1;
  public String doWhileStatementDescription = "Do While Statement";
  public int emptyStatement = 0;
  public String emptyStatementDescription = "Empty Statement";
  public int expressionList = 0;
  public String expressionListDescription = "Expression List";
  public int expressionListStatement = 0;
  public String expressionListStatementDescription = "Expression List Statement";
  public int expressionStatement = 0;
  public String expressionStatementDescription = "Expression Statement";
  public int field = 0;
  public String fieldDescription = "Field";
  public int forStatement = 1;
  public String forStatementDescription = "For Statement";
  public int foreachStatement = 1;
  public String foreachStatementDescription = "Foreach Statement";
  public int ifStatement = 1;
  public String ifStatementDescription = "If Statement";
  public int importList = 0;
  public String importListDescription = "Import List";
  public int importStatement = 0;
  public String importStatementDescription = "Import Statement";
  public int importStaticStatement = 0;
  public String importStaticStatementDescription = "Import Static Statement";
  public int inlineDocTag = 0;
  public String inlineDocTagDescription = "Inline Doc Tag";
  public int instanceOfExpression = 0;
  public String instanceOfExpressionDescription = "Instance Of Expression";
  public int labeledStatement = 1;
  public String labeledStatementDescription = "Labeled Statement";
  public int literalExpression = 0;
  public String literalExpressionDescription = "Literal Expression";
  public int localVariable = 0;
  public String localVariableDescription = "Local Variable";
  public int method = 1;
  public String methodDescription = "Method";
  public int methodCallExpression = 0;
  public String methodCallExpressionDescription = "Method Call Expression";
  public int modifierList = 0;
  public String modifierListDescription = "Modifier List";
  public int newExpression = 0;
  public String newExpressionDescription = "New Expression";
  public int packageStatement = 0;
  public String packageStatementDescription = "Package Statement";
  public int parameter = 0;
  public String parameterDescription = "Parameter";
  public int receiverParameter = 0;
  public String receiverParameterDescription = "Receiver Parameter";
  public int parameterList = 0;
  public String parameterListDescription = "Parameter List";
  public int postfixExpression = 0;
  public String postfixExpressionDescription = "Postfix Expression";
  public int prefixExpression = 0;
  public String prefixExpressionDescription = "Prefix Expression";
  public int referenceParameterList = 0;
  public String referenceParameterListDescription = "Reference Parameter List";
  public int typeParameterList = 0;
  public String typeParameterListDescription = "Type Parameter List";
  public int returnStatement = 1;
  public String returnStatementDescription = "Return Statement";
  public int superExpression = 0;
  public String superExpressionDescription = "Super Expression";
  public int switchLabelStatement = 0;
  public String switchLabelStatementDescription = "Switch Label Statement";
  public int switchStatement = 1;
  public String switchStatementDescription = "Switch Statement";
  public int synchronizedStatement = 0;
  public String synchronizedStatementDescription = "Synchronized Statement";
  public int thisExpression = 0;
  public String thisExpressionDescription = "This Expression";
  public int throwStatement = 1;
  public String throwStatementDescription = "Throw Statement";
  public int tryStatement = 0;
  public String tryStatementDescription = "Try Statement";
  public int catchSection = 1;
  public String catchSectionDescription = "Catch Section";
  public int resourceList = 0;
  public String resourceListDescription = "Resource List";
  public int resourceVariable = 0;
  public String resourceVariableDescription = "Resource Variable";
  public int resourceExpression = 0;
  public String resourceExpressionDescription = "Resource Expression";
  public int typeCastExpression = 0;
  public String typeCastExpressionDescription = "Type Cast Expression";
  public int whileStatement = 1;
  public String whileStatementDescription = "While Statement";
  public int typeParameter = 0;
  public String typeParameterDescription = "Type Parameter";
  public int annotation = 0;
  public String annotationDescription = "Annotation";
  public int annotationParameterList = 0;
  public String annotationParameterListDescription = "Annotation Parameter List";
  public int annotationArrayInitializer = 0;
  public String annotationArrayInitializerDescription = "Annotation Array Initializer";
  public int nameValuePair = 0;
  public String nameValuePairDescription = "Name Value Pair";
  public int annotationMethod = 0;
  public String annotationMethodDescription = "Annotation Method";
  public int enumConstant = 0;
  public String enumConstantDescription = "Enum Constant";
  public int enumConstantInitializer = 0;
  public String enumConstantInitializerDescription = "Enum Constant Initializer";
  public int polyadicExpression = 1;
  public String polyadicExpressionDescription = "Polyadic Expression";
  public int lambdaExpression = 1;
  public String lambdaExpressionDescription = "Lambda Expression";
  public int module = 0;
  public String moduleDescription = "Module";
  public int requiresStatement = 0;
  public String requiresStatementDescription = "Requires Statement";
  public int usesStatement = 0;
  public String usesStatementDescription = "Uses Statement";
  public int providesStatement = 0;
  public String providesStatementDescription = "Provides Statement";
  public int methodRefExpression = 1;
  public String methodRefExpressionDescription = "Method Reference Expression";
  public int type = 0;
  public String typeDescription = "Type";
  public int diamondType = 0;
  public String diamondTypeDescription = "Diamond Type";
  public int importStaticReference = 0;
  public String importStaticReferenceDescription = "Import Static Reference";
  public int providesWithList = 0;
  public String providesWithListDescription = "Provides With List";
  public int opensStatement = 0;
  public String opensStatementDescription = "Opens Statement";
  public int exportsStatement = 0;
  public String exportsStatementDescription = "Exports Statement";
  public int throwsList = 0;
  public String throwsListDescription = "Throws List";
  public int extendsBoundList = 0;
  public String extendsBoundListDescription = "Extends Bound List";
  public int implementsList = 0;
  public String implementsListDescription = "Implements List";
  public int extendsList = 0;
  public String extendsListDescription = "Extends List";
  public int emptyExpression = 0;
  public String emptyExpressionDescription = "Empty Expression";
  public int switchExpression = 0;
  public String switchExpressionDescription = "Switch Expression";
  public int switchLabeledRule = 0;
  public String switchLabeledRuleDescription = "Switch Labeled Rule";
  public int moduleReference = 0;
  public String moduleReferenceDescription = "Module Reference";
  public int javaCodeReference = 0;
  public String javaCodeReferenceDescription = "JavaCode Reference";
  public int referenceExpression = 0;
  public String referenceExpressionDescription = "Reference Expression";
  public int parenthExpression = 0;
  public String parenthExpressionDescription = "Parenthesized Expression";
  public int docMethodOrFieldRef = 0;
  public String docMethodOrFieldRefDescription = "Documentation Method Or Field Ref";
  public int docParameterRef = 0;
  public String docParameterRefDescription = "Documentation Parameter Ref";
  public int docTagValueElement = 0;
  public String docTagValueElementDescription = "Documentation Tag Value Element";
  public int docReferenceHolder = 0;
  public String docReferenceHolderDescription = "Documentation Reference Holder";
  public int docTypeHolder = 0;
  public String docTypeHolderDescription = "Documentation Type Holder";

  // Kotlin specific settings
  public boolean metricsForKotlinClass = true;
  public boolean metricsForKotlinFunction = true;
  public boolean metricsForKotlinProperty = false;
  public boolean metricsForKotlinLambda = true;

  // Kotlin control flow visibility flags (consistent with Java)
  public boolean metricsForKotlinIf = true;
  public boolean metricsForKotlinWhen = true;
  public boolean metricsForKotlinFor = true;
  public boolean metricsForKotlinWhile = true;
  public boolean metricsForKotlinTry = true;
  
  // Kotlin complexity values
  public int kotlinClass = 1;
  public String kotlinClassDescription = "Kotlin Class";
  public int kotlinFunction = 1;
  public String kotlinFunctionDescription = "Kotlin Function";
  public int kotlinProperty = 0;
  public String kotlinPropertyDescription = "Kotlin Property";
  public int kotlinIfExpression = 1;
  public String kotlinIfExpressionDescription = "Kotlin If Expression";
  public int kotlinWhenExpression = 1;
  public String kotlinWhenExpressionDescription = "Kotlin When Expression";
  public int kotlinForLoop = 1;
  public String kotlinForLoopDescription = "Kotlin For Loop";
  public int kotlinWhileLoop = 1;
  public String kotlinWhileLoopDescription = "Kotlin While Loop";
  public int kotlinTryExpression = 1;
  public String kotlinTryExpressionDescription = "Kotlin Try Expression";
  public int kotlinLambdaExpression = 1;
  public String kotlinLambdaExpressionDescription = "Kotlin Lambda Expression";
  public int kotlinElvisExpression = 1;
  public String kotlinElvisExpressionDescription = "Kotlin Elvis Expression";
}
