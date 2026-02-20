package com.github.ehs208.codemetrics.configuration;

import com.github.ehs208.codemetrics.core.config.MetricsConfiguration;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditorConfig implements Configurable, Configurable.NoScroll {
  private final MetricsConfiguration configuration;
  private final MetricsConfiguration baseConfiguration = new MetricsConfiguration();
  private java.util.List<BeanField> basicFields;
  private java.util.List<BeanField> advancedFields;
  private java.util.List<BeanField> miscFields;

  protected EditorConfig() {
    configuration = MetricsConfiguration.getInstance();
    basicFields = new ArrayList<>();
    advancedFields = new ArrayList<>();
    miscFields = new ArrayList<>();

    /* basic fields */
    this.colorPicker(
        basicFields,
        () -> configuration.complexityColorLow,
        v -> configuration.complexityColorLow = v,
        "Color: Low");
    colorPicker(
        basicFields,
        () -> configuration.complexityColorNormal,
        v -> configuration.complexityColorNormal = v,
        "Color: Normal");
    colorPicker(
        basicFields,
        () -> configuration.complexityColorHigh,
        v -> configuration.complexityColorHigh = v,
        "Color: High");
    colorPicker(
        basicFields,
        () -> configuration.complexityColorExtreme,
        v -> configuration.complexityColorExtreme = v,
        "Color: Extreme");

    numeric(
        basicFields,
        () -> configuration.complexityLevelLow,
        v -> configuration.complexityLevelLow = v,
        "Threshold: Low");
    numeric(
        basicFields,
        () -> configuration.complexityLevelNormal,
        v -> configuration.complexityLevelNormal = v,
        "Threshold: Normal");
    numeric(
        basicFields,
        () -> configuration.complexityLevelHigh,
        v -> configuration.complexityLevelHigh = v,
        "Threshold: High");
    numeric(
        basicFields,
        () -> configuration.complexityLevelExtreme,
        v -> configuration.complexityLevelExtreme = v,
        "Threshold: Extreme");

    numeric(
        basicFields,
        () -> configuration.hiddenUnder,
        v -> configuration.hiddenUnder = v,
        "Minimum complexity to display");

    checkBox(
        basicFields,
        () -> configuration.metricsForAnonymousClass,
        v -> configuration.metricsForAnonymousClass = v,
        "Show metrics for Java anonymous classes");
    checkBox(
        basicFields,
        () -> configuration.metricsForAClass,
        v -> configuration.metricsForAClass = v,
        "Show metrics for Java classes");
    checkBox(
        basicFields,
        () -> configuration.metricsForMethod,
        v -> configuration.metricsForMethod = v,
        "Show metrics for Java methods");
    checkBox(
        basicFields,
        () -> configuration.metricsForLambdaExpression,
        v -> configuration.metricsForLambdaExpression = v,
        "Show metrics for Java lambda expressions");

    // Kotlin-specific basic settings
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinClass,
        v -> configuration.metricsForKotlinClass = v,
        "Show metrics for Kotlin classes");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinFunction,
        v -> configuration.metricsForKotlinFunction = v,
        "Show metrics for Kotlin functions");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinProperty,
        v -> configuration.metricsForKotlinProperty = v,
        "Show metrics for Kotlin properties");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinLambda,
        v -> configuration.metricsForKotlinLambda = v,
        "Show metrics for Kotlin lambda expressions");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinIf,
        v -> configuration.metricsForKotlinIf = v,
        "Show metrics for Kotlin if expressions");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinWhen,
        v -> configuration.metricsForKotlinWhen = v,
        "Show metrics for Kotlin when expressions");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinFor,
        v -> configuration.metricsForKotlinFor = v,
        "Show metrics for Kotlin for loops");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinWhile,
        v -> configuration.metricsForKotlinWhile = v,
        "Show metrics for Kotlin while loops");
    checkBox(
        basicFields,
        () -> configuration.metricsForKotlinTry,
        v -> configuration.metricsForKotlinTry = v,
        "Show metrics for Kotlin try expressions");

    /* advanced fields */
    text(
        advancedFields,
        () -> configuration.complexityLevelExtremeDescription,
        (v) -> configuration.complexityLevelExtremeDescription = v,
        "Description: Extreme");
    text(
        advancedFields,
        () -> configuration.complexityLevelHighDescription,
        (v) -> configuration.complexityLevelHighDescription = v,
        "Description: High");
    text(
        advancedFields,
        () -> configuration.complexityLevelNormalDescription,
        (v) -> configuration.complexityLevelNormalDescription = v,
        "Description: Normal");
    text(
        advancedFields,
        () -> configuration.complexityLevelLowDescription,
        (v) -> configuration.complexityLevelLowDescription = v,
        "Description: Low");
    text(
        advancedFields,
        () -> configuration.complexityTemplate,
        (v) -> configuration.complexityTemplate = v,
        "Complexity display template");

    numeric(
        advancedFields,
        () -> configuration.anonymousClass,
        v -> configuration.anonymousClass = v,
        baseConfiguration.anonymousClassDescription);
    numeric(
        advancedFields,
        () -> configuration.arrayAccessExpression,
        v -> configuration.arrayAccessExpression = v,
        baseConfiguration.arrayAccessExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.arrayInitializerExpression,
        v -> configuration.arrayInitializerExpression = v,
        baseConfiguration.arrayInitializerExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.assertStatement,
        v -> configuration.assertStatement = v,
        baseConfiguration.assertStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.assignmentExpression,
        v -> configuration.assignmentExpression = v,
        baseConfiguration.assignmentExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.binaryExpression,
        v -> configuration.binaryExpression = v,
        baseConfiguration.binaryExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.blockStatement,
        v -> configuration.blockStatement = v,
        baseConfiguration.blockStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.breakStatement,
        v -> configuration.breakStatement = v,
        baseConfiguration.breakStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.aClass,
        v -> configuration.aClass = v,
        baseConfiguration.aClassDescription);
    numeric(
        advancedFields,
        () -> configuration.classInitializer,
        v -> configuration.classInitializer = v,
        baseConfiguration.classInitializerDescription);
    numeric(
        advancedFields,
        () -> configuration.classObjectAccessExpression,
        v -> configuration.classObjectAccessExpression = v,
        baseConfiguration.classObjectAccessExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.codeBlock,
        v -> configuration.codeBlock = v,
        baseConfiguration.codeBlockDescription);
    numeric(
        advancedFields,
        () -> configuration.conditionalExpression,
        v -> configuration.conditionalExpression = v,
        baseConfiguration.conditionalExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.continueStatement,
        v -> configuration.continueStatement = v,
        baseConfiguration.continueStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.declarationStatement,
        v -> configuration.declarationStatement = v,
        baseConfiguration.declarationStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.docComment,
        v -> configuration.docComment = v,
        baseConfiguration.docCommentDescription);
    numeric(
        advancedFields,
        () -> configuration.docTag,
        v -> configuration.docTag = v,
        baseConfiguration.docTagDescription);
    numeric(
        advancedFields,
        () -> configuration.doWhileStatement,
        v -> configuration.doWhileStatement = v,
        baseConfiguration.doWhileStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.emptyStatement,
        v -> configuration.emptyStatement = v,
        baseConfiguration.emptyStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.expressionList,
        v -> configuration.expressionList = v,
        baseConfiguration.expressionListDescription);
    numeric(
        advancedFields,
        () -> configuration.expressionListStatement,
        v -> configuration.expressionListStatement = v,
        baseConfiguration.expressionListStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.expressionStatement,
        v -> configuration.expressionStatement = v,
        baseConfiguration.expressionStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.field,
        v -> configuration.field = v,
        baseConfiguration.fieldDescription);
    numeric(
        advancedFields,
        () -> configuration.forStatement,
        v -> configuration.forStatement = v,
        baseConfiguration.forStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.foreachStatement,
        v -> configuration.foreachStatement = v,
        baseConfiguration.foreachStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.ifStatement,
        v -> configuration.ifStatement = v,
        baseConfiguration.ifStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.importList,
        v -> configuration.importList = v,
        baseConfiguration.importListDescription);
    numeric(
        advancedFields,
        () -> configuration.importStatement,
        v -> configuration.importStatement = v,
        baseConfiguration.importStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.importStaticStatement,
        v -> configuration.importStaticStatement = v,
        baseConfiguration.importStaticStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.inlineDocTag,
        v -> configuration.inlineDocTag = v,
        baseConfiguration.inlineDocTagDescription);
    numeric(
        advancedFields,
        () -> configuration.instanceOfExpression,
        v -> configuration.instanceOfExpression = v,
        baseConfiguration.instanceOfExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.labeledStatement,
        v -> configuration.labeledStatement = v,
        baseConfiguration.labeledStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.literalExpression,
        v -> configuration.literalExpression = v,
        baseConfiguration.literalExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.localVariable,
        v -> configuration.localVariable = v,
        baseConfiguration.localVariableDescription);
    numeric(
        advancedFields,
        () -> configuration.method,
        v -> configuration.method = v,
        baseConfiguration.methodDescription);
    numeric(
        advancedFields,
        () -> configuration.methodCallExpression,
        v -> configuration.methodCallExpression = v,
        baseConfiguration.methodCallExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.modifierList,
        v -> configuration.modifierList = v,
        baseConfiguration.modifierListDescription);
    numeric(
        advancedFields,
        () -> configuration.newExpression,
        v -> configuration.newExpression = v,
        baseConfiguration.newExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.packageStatement,
        v -> configuration.packageStatement = v,
        baseConfiguration.packageStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.parameter,
        v -> configuration.parameter = v,
        baseConfiguration.parameterDescription);
    numeric(
        advancedFields,
        () -> configuration.receiverParameter,
        v -> configuration.receiverParameter = v,
        baseConfiguration.receiverParameterDescription);
    numeric(
        advancedFields,
        () -> configuration.parameterList,
        v -> configuration.parameterList = v,
        baseConfiguration.parameterListDescription);
    numeric(
        advancedFields,
        () -> configuration.postfixExpression,
        v -> configuration.postfixExpression = v,
        baseConfiguration.postfixExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.prefixExpression,
        v -> configuration.prefixExpression = v,
        baseConfiguration.prefixExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.referenceParameterList,
        v -> configuration.referenceParameterList = v,
        baseConfiguration.referenceParameterListDescription);
    numeric(
        advancedFields,
        () -> configuration.typeParameterList,
        v -> configuration.typeParameterList = v,
        baseConfiguration.typeParameterListDescription);
    numeric(
        advancedFields,
        () -> configuration.returnStatement,
        v -> configuration.returnStatement = v,
        baseConfiguration.returnStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.superExpression,
        v -> configuration.superExpression = v,
        baseConfiguration.superExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.switchLabelStatement,
        v -> configuration.switchLabelStatement = v,
        baseConfiguration.switchLabelStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.switchStatement,
        v -> configuration.switchStatement = v,
        baseConfiguration.switchStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.synchronizedStatement,
        v -> configuration.synchronizedStatement = v,
        baseConfiguration.synchronizedStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.thisExpression,
        v -> configuration.thisExpression = v,
        baseConfiguration.thisExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.throwStatement,
        v -> configuration.throwStatement = v,
        baseConfiguration.throwStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.tryStatement,
        v -> configuration.tryStatement = v,
        baseConfiguration.tryStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.catchSection,
        v -> configuration.catchSection = v,
        baseConfiguration.catchSectionDescription);
    numeric(
        advancedFields,
        () -> configuration.resourceList,
        v -> configuration.resourceList = v,
        baseConfiguration.resourceListDescription);
    numeric(
        advancedFields,
        () -> configuration.resourceVariable,
        v -> configuration.resourceVariable = v,
        baseConfiguration.resourceVariableDescription);
    numeric(
        advancedFields,
        () -> configuration.resourceExpression,
        v -> configuration.resourceExpression = v,
        baseConfiguration.resourceExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.typeCastExpression,
        v -> configuration.typeCastExpression = v,
        baseConfiguration.typeCastExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.whileStatement,
        v -> configuration.whileStatement = v,
        baseConfiguration.whileStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.typeParameter,
        v -> configuration.typeParameter = v,
        baseConfiguration.typeParameterDescription);
    numeric(
        advancedFields,
        () -> configuration.annotation,
        v -> configuration.annotation = v,
        baseConfiguration.annotationDescription);
    numeric(
        advancedFields,
        () -> configuration.annotationParameterList,
        v -> configuration.annotationParameterList = v,
        baseConfiguration.annotationParameterListDescription);
    numeric(
        advancedFields,
        () -> configuration.annotationArrayInitializer,
        v -> configuration.annotationArrayInitializer = v,
        baseConfiguration.annotationArrayInitializerDescription);
    numeric(
        advancedFields,
        () -> configuration.nameValuePair,
        v -> configuration.nameValuePair = v,
        baseConfiguration.nameValuePairDescription);
    numeric(
        advancedFields,
        () -> configuration.annotationMethod,
        v -> configuration.annotationMethod = v,
        baseConfiguration.annotationMethodDescription);
    numeric(
        advancedFields,
        () -> configuration.enumConstant,
        v -> configuration.enumConstant = v,
        baseConfiguration.enumConstantDescription);
    numeric(
        advancedFields,
        () -> configuration.enumConstantInitializer,
        v -> configuration.enumConstantInitializer = v,
        baseConfiguration.enumConstantInitializerDescription);
    numeric(
        advancedFields,
        () -> configuration.polyadicExpression,
        v -> configuration.polyadicExpression = v,
        baseConfiguration.polyadicExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.lambdaExpression,
        v -> configuration.lambdaExpression = v,
        baseConfiguration.lambdaExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.module,
        v -> configuration.module = v,
        baseConfiguration.moduleDescription);
    numeric(
        advancedFields,
        () -> configuration.requiresStatement,
        v -> configuration.requiresStatement = v,
        baseConfiguration.requiresStatementDescription);

    numeric(
        advancedFields,
        () -> configuration.usesStatement,
        v -> configuration.usesStatement = v,
        baseConfiguration.usesStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.providesStatement,
        v -> configuration.providesStatement = v,
        baseConfiguration.providesStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.methodRefExpression,
        v -> configuration.methodRefExpression = v,
        baseConfiguration.methodRefExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.type,
        v -> configuration.type = v,
        baseConfiguration.typeDescription);
    numeric(
        advancedFields,
        () -> configuration.diamondType,
        v -> configuration.diamondType = v,
        baseConfiguration.diamondTypeDescription);
    numeric(
        advancedFields,
        () -> configuration.importStaticReference,
        v -> configuration.importStaticReference = v,
        baseConfiguration.importStaticReferenceDescription);
    numeric(
        advancedFields,
        () -> configuration.providesWithList,
        v -> configuration.providesWithList = v,
        baseConfiguration.providesWithListDescription);
    numeric(
        advancedFields,
        () -> configuration.opensStatement,
        v -> configuration.opensStatement = v,
        baseConfiguration.opensStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.exportsStatement,
        v -> configuration.exportsStatement = v,
        baseConfiguration.exportsStatementDescription);
    numeric(
        advancedFields,
        () -> configuration.throwsList,
        v -> configuration.throwsList = v,
        baseConfiguration.throwsListDescription);
    numeric(
        advancedFields,
        () -> configuration.extendsBoundList,
        v -> configuration.extendsBoundList = v,
        baseConfiguration.extendsBoundListDescription);
    numeric(
        advancedFields,
        () -> configuration.implementsList,
        v -> configuration.implementsList = v,
        baseConfiguration.implementsListDescription);
    numeric(
        advancedFields,
        () -> configuration.extendsList,
        v -> configuration.extendsList = v,
        baseConfiguration.extendsListDescription);
    numeric(
        advancedFields,
        () -> configuration.emptyExpression,
        v -> configuration.emptyExpression = v,
        baseConfiguration.emptyExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.switchExpression,
        v -> configuration.switchExpression = v,
        baseConfiguration.switchExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.switchLabeledRule,
        v -> configuration.switchLabeledRule = v,
        baseConfiguration.switchLabeledRuleDescription);
    numeric(
        advancedFields,
        () -> configuration.moduleReference,
        v -> configuration.moduleReference = v,
        baseConfiguration.moduleReferenceDescription);
    numeric(
        advancedFields,
        () -> configuration.javaCodeReference,
        v -> configuration.javaCodeReference = v,
        baseConfiguration.javaCodeReferenceDescription);
    numeric(
        advancedFields,
        () -> configuration.referenceExpression,
        v -> configuration.referenceExpression = v,
        baseConfiguration.referenceExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.parenthExpression,
        v -> configuration.parenthExpression = v,
        baseConfiguration.parenthExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.docMethodOrFieldRef,
        v -> configuration.docMethodOrFieldRef = v,
        baseConfiguration.docMethodOrFieldRefDescription);
    numeric(
        advancedFields,
        () -> configuration.docParameterRef,
        v -> configuration.docParameterRef = v,
        baseConfiguration.docParameterRefDescription);
    numeric(
        advancedFields,
        () -> configuration.docTagValueElement,
        v -> configuration.docTagValueElement = v,
        baseConfiguration.docTagValueElementDescription);
    numeric(
        advancedFields,
        () -> configuration.docReferenceHolder,
        v -> configuration.docReferenceHolder = v,
        baseConfiguration.docReferenceHolderDescription);
    numeric(
        advancedFields,
        () -> configuration.docTypeHolder,
        v -> configuration.docTypeHolder = v,
        baseConfiguration.docTypeHolderDescription);

    // Kotlin specific numeric fields
    numeric(
        advancedFields,
        () -> configuration.kotlinClass,
        v -> configuration.kotlinClass = v,
        baseConfiguration.kotlinClassDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinFunction,
        v -> configuration.kotlinFunction = v,
        baseConfiguration.kotlinFunctionDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinProperty,
        v -> configuration.kotlinProperty = v,
        baseConfiguration.kotlinPropertyDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinIfExpression,
        v -> configuration.kotlinIfExpression = v,
        baseConfiguration.kotlinIfExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinWhenExpression,
        v -> configuration.kotlinWhenExpression = v,
        baseConfiguration.kotlinWhenExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinForLoop,
        v -> configuration.kotlinForLoop = v,
        baseConfiguration.kotlinForLoopDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinWhileLoop,
        v -> configuration.kotlinWhileLoop = v,
        baseConfiguration.kotlinWhileLoopDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinTryExpression,
        v -> configuration.kotlinTryExpression = v,
        baseConfiguration.kotlinTryExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinLambdaExpression,
        v -> configuration.kotlinLambdaExpression = v,
        baseConfiguration.kotlinLambdaExpressionDescription);
    numeric(
        advancedFields,
        () -> configuration.kotlinElvisExpression,
        v -> configuration.kotlinElvisExpression = v,
        baseConfiguration.kotlinElvisExpressionDescription);

    text(
        miscFields,
        () -> configuration.anonymousClassDescription,
        v -> configuration.anonymousClassDescription = v,
        baseConfiguration.anonymousClassDescription);
    text(
        miscFields,
        () -> configuration.arrayAccessExpressionDescription,
        v -> configuration.arrayAccessExpressionDescription = v,
        baseConfiguration.arrayAccessExpressionDescription);
    text(
        miscFields,
        () -> configuration.arrayInitializerExpressionDescription,
        v -> configuration.arrayInitializerExpressionDescription = v,
        baseConfiguration.arrayInitializerExpressionDescription);
    text(
        miscFields,
        () -> configuration.assertStatementDescription,
        v -> configuration.assertStatementDescription = v,
        baseConfiguration.assertStatementDescription);
    text(
        miscFields,
        () -> configuration.assignmentExpressionDescription,
        v -> configuration.assignmentExpressionDescription = v,
        baseConfiguration.assignmentExpressionDescription);
    text(
        miscFields,
        () -> configuration.binaryExpressionDescription,
        v -> configuration.binaryExpressionDescription = v,
        baseConfiguration.binaryExpressionDescription);
    text(
        miscFields,
        () -> configuration.blockStatementDescription,
        v -> configuration.blockStatementDescription = v,
        baseConfiguration.blockStatementDescription);
    text(
        miscFields,
        () -> configuration.breakStatementDescription,
        v -> configuration.breakStatementDescription = v,
        baseConfiguration.breakStatementDescription);
    text(
        miscFields,
        () -> configuration.aClassDescription,
        v -> configuration.aClassDescription = v,
        baseConfiguration.aClassDescription);
    text(
        miscFields,
        () -> configuration.classInitializerDescription,
        v -> configuration.classInitializerDescription = v,
        baseConfiguration.classInitializerDescription);
    text(
        miscFields,
        () -> configuration.classObjectAccessExpressionDescription,
        v -> configuration.classObjectAccessExpressionDescription = v,
        baseConfiguration.classObjectAccessExpressionDescription);
    text(
        miscFields,
        () -> configuration.codeBlockDescription,
        v -> configuration.codeBlockDescription = v,
        baseConfiguration.codeBlockDescription);
    text(
        miscFields,
        () -> configuration.conditionalExpressionDescription,
        v -> configuration.conditionalExpressionDescription = v,
        baseConfiguration.conditionalExpressionDescription);
    text(
        miscFields,
        () -> configuration.continueStatementDescription,
        v -> configuration.continueStatementDescription = v,
        baseConfiguration.continueStatementDescription);
    text(
        miscFields,
        () -> configuration.declarationStatementDescription,
        v -> configuration.declarationStatementDescription = v,
        baseConfiguration.declarationStatementDescription);
    text(
        miscFields,
        () -> configuration.docCommentDescription,
        v -> configuration.docCommentDescription = v,
        baseConfiguration.docCommentDescription);
    text(
        miscFields,
        () -> configuration.docTagDescription,
        v -> configuration.docTagDescription = v,
        baseConfiguration.docTagDescription);
    text(
        miscFields,
        () -> configuration.doWhileStatementDescription,
        v -> configuration.doWhileStatementDescription = v,
        baseConfiguration.doWhileStatementDescription);
    text(
        miscFields,
        () -> configuration.emptyStatementDescription,
        v -> configuration.emptyStatementDescription = v,
        baseConfiguration.emptyStatementDescription);
    text(
        miscFields,
        () -> configuration.expressionListDescription,
        v -> configuration.expressionListDescription = v,
        baseConfiguration.expressionListDescription);
    text(
        miscFields,
        () -> configuration.expressionListStatementDescription,
        v -> configuration.expressionListStatementDescription = v,
        baseConfiguration.expressionListStatementDescription);
    text(
        miscFields,
        () -> configuration.expressionStatementDescription,
        v -> configuration.expressionStatementDescription = v,
        baseConfiguration.expressionStatementDescription);
    text(
        miscFields,
        () -> configuration.fieldDescription,
        v -> configuration.fieldDescription = v,
        baseConfiguration.fieldDescription);
    text(
        miscFields,
        () -> configuration.forStatementDescription,
        v -> configuration.forStatementDescription = v,
        baseConfiguration.forStatementDescription);
    text(
        miscFields,
        () -> configuration.foreachStatementDescription,
        v -> configuration.foreachStatementDescription = v,
        baseConfiguration.foreachStatementDescription);
    text(
        miscFields,
        () -> configuration.ifStatementDescription,
        v -> configuration.ifStatementDescription = v,
        baseConfiguration.ifStatementDescription);
    text(
        miscFields,
        () -> configuration.importListDescription,
        v -> configuration.importListDescription = v,
        baseConfiguration.importListDescription);
    text(
        miscFields,
        () -> configuration.importStatementDescription,
        v -> configuration.importStatementDescription = v,
        baseConfiguration.importStatementDescription);
    text(
        miscFields,
        () -> configuration.importStaticStatementDescription,
        v -> configuration.importStaticStatementDescription = v,
        baseConfiguration.importStaticStatementDescription);
    text(
        miscFields,
        () -> configuration.inlineDocTagDescription,
        v -> configuration.inlineDocTagDescription = v,
        baseConfiguration.inlineDocTagDescription);
    text(
        miscFields,
        () -> configuration.instanceOfExpressionDescription,
        v -> configuration.instanceOfExpressionDescription = v,
        baseConfiguration.instanceOfExpressionDescription);
    text(
        miscFields,
        () -> configuration.labeledStatementDescription,
        v -> configuration.labeledStatementDescription = v,
        baseConfiguration.labeledStatementDescription);
    text(
        miscFields,
        () -> configuration.literalExpressionDescription,
        v -> configuration.literalExpressionDescription = v,
        baseConfiguration.literalExpressionDescription);
    text(
        miscFields,
        () -> configuration.localVariableDescription,
        v -> configuration.localVariableDescription = v,
        baseConfiguration.localVariableDescription);
    text(
        miscFields,
        () -> configuration.methodDescription,
        v -> configuration.methodDescription = v,
        baseConfiguration.methodDescription);
    text(
        miscFields,
        () -> configuration.methodCallExpressionDescription,
        v -> configuration.methodCallExpressionDescription = v,
        baseConfiguration.methodCallExpressionDescription);
    text(
        miscFields,
        () -> configuration.modifierListDescription,
        v -> configuration.modifierListDescription = v,
        baseConfiguration.modifierListDescription);
    text(
        miscFields,
        () -> configuration.newExpressionDescription,
        v -> configuration.newExpressionDescription = v,
        baseConfiguration.newExpressionDescription);
    text(
        miscFields,
        () -> configuration.packageStatementDescription,
        v -> configuration.packageStatementDescription = v,
        baseConfiguration.packageStatementDescription);
    text(
        miscFields,
        () -> configuration.parameterDescription,
        v -> configuration.parameterDescription = v,
        baseConfiguration.parameterDescription);
    text(
        miscFields,
        () -> configuration.receiverParameterDescription,
        v -> configuration.receiverParameterDescription = v,
        baseConfiguration.receiverParameterDescription);
    text(
        miscFields,
        () -> configuration.parameterListDescription,
        v -> configuration.parameterListDescription = v,
        baseConfiguration.parameterListDescription);
    text(
        miscFields,
        () -> configuration.postfixExpressionDescription,
        v -> configuration.postfixExpressionDescription = v,
        baseConfiguration.postfixExpressionDescription);
    text(
        miscFields,
        () -> configuration.prefixExpressionDescription,
        v -> configuration.prefixExpressionDescription = v,
        baseConfiguration.prefixExpressionDescription);
    text(
        miscFields,
        () -> configuration.referenceParameterListDescription,
        v -> configuration.referenceParameterListDescription = v,
        baseConfiguration.referenceParameterListDescription);
    text(
        miscFields,
        () -> configuration.typeParameterListDescription,
        v -> configuration.typeParameterListDescription = v,
        baseConfiguration.typeParameterListDescription);
    text(
        miscFields,
        () -> configuration.returnStatementDescription,
        v -> configuration.returnStatementDescription = v,
        baseConfiguration.returnStatementDescription);
    text(
        miscFields,
        () -> configuration.superExpressionDescription,
        v -> configuration.superExpressionDescription = v,
        baseConfiguration.superExpressionDescription);
    text(
        miscFields,
        () -> configuration.switchLabelStatementDescription,
        v -> configuration.switchLabelStatementDescription = v,
        baseConfiguration.switchLabelStatementDescription);
    text(
        miscFields,
        () -> configuration.switchStatementDescription,
        v -> configuration.switchStatementDescription = v,
        baseConfiguration.switchStatementDescription);
    text(
        miscFields,
        () -> configuration.synchronizedStatementDescription,
        v -> configuration.synchronizedStatementDescription = v,
        baseConfiguration.synchronizedStatementDescription);
    text(
        miscFields,
        () -> configuration.thisExpressionDescription,
        v -> configuration.thisExpressionDescription = v,
        baseConfiguration.thisExpressionDescription);
    text(
        miscFields,
        () -> configuration.throwStatementDescription,
        v -> configuration.throwStatementDescription = v,
        baseConfiguration.throwStatementDescription);
    text(
        miscFields,
        () -> configuration.tryStatementDescription,
        v -> configuration.tryStatementDescription = v,
        baseConfiguration.tryStatementDescription);
    text(
        miscFields,
        () -> configuration.catchSectionDescription,
        v -> configuration.catchSectionDescription = v,
        baseConfiguration.catchSectionDescription);
    text(
        miscFields,
        () -> configuration.resourceListDescription,
        v -> configuration.resourceListDescription = v,
        baseConfiguration.resourceListDescription);
    text(
        miscFields,
        () -> configuration.resourceVariableDescription,
        v -> configuration.resourceVariableDescription = v,
        baseConfiguration.resourceVariableDescription);
    text(
        miscFields,
        () -> configuration.resourceExpressionDescription,
        v -> configuration.resourceExpressionDescription = v,
        baseConfiguration.resourceExpressionDescription);
    text(
        miscFields,
        () -> configuration.typeCastExpressionDescription,
        v -> configuration.typeCastExpressionDescription = v,
        baseConfiguration.typeCastExpressionDescription);
    text(
        miscFields,
        () -> configuration.whileStatementDescription,
        v -> configuration.whileStatementDescription = v,
        baseConfiguration.whileStatementDescription);
    text(
        miscFields,
        () -> configuration.typeParameterDescription,
        v -> configuration.typeParameterDescription = v,
        baseConfiguration.typeParameterDescription);
    text(
        miscFields,
        () -> configuration.annotationDescription,
        v -> configuration.annotationDescription = v,
        baseConfiguration.annotationDescription);
    text(
        miscFields,
        () -> configuration.annotationParameterListDescription,
        v -> configuration.annotationParameterListDescription = v,
        baseConfiguration.annotationParameterListDescription);
    text(
        miscFields,
        () -> configuration.annotationArrayInitializerDescription,
        v -> configuration.annotationArrayInitializerDescription = v,
        baseConfiguration.annotationArrayInitializerDescription);
    text(
        miscFields,
        () -> configuration.nameValuePairDescription,
        v -> configuration.nameValuePairDescription = v,
        baseConfiguration.nameValuePairDescription);
    text(
        miscFields,
        () -> configuration.annotationMethodDescription,
        v -> configuration.annotationMethodDescription = v,
        baseConfiguration.annotationMethodDescription);
    text(
        miscFields,
        () -> configuration.enumConstantDescription,
        v -> configuration.enumConstantDescription = v,
        baseConfiguration.enumConstantDescription);
    text(
        miscFields,
        () -> configuration.enumConstantInitializerDescription,
        v -> configuration.enumConstantInitializerDescription = v,
        baseConfiguration.enumConstantInitializerDescription);
    text(
        miscFields,
        () -> configuration.polyadicExpressionDescription,
        v -> configuration.polyadicExpressionDescription = v,
        baseConfiguration.polyadicExpressionDescription);
    text(
        miscFields,
        () -> configuration.lambdaExpressionDescription,
        v -> configuration.lambdaExpressionDescription = v,
        baseConfiguration.lambdaExpressionDescription);
    text(
        miscFields,
        () -> configuration.moduleDescription,
        v -> configuration.moduleDescription = v,
        baseConfiguration.moduleDescription);
    text(
        miscFields,
        () -> configuration.requiresStatementDescription,
        v -> configuration.requiresStatementDescription = v,
        baseConfiguration.requiresStatementDescription);
    text(
        miscFields,
        () -> configuration.usesStatementDescription,
        v -> configuration.usesStatementDescription = v,
        baseConfiguration.usesStatementDescription);
    text(
        miscFields,
        () -> configuration.providesStatementDescription,
        v -> configuration.providesStatementDescription = v,
        baseConfiguration.providesStatementDescription);
    text(
        miscFields,
        () -> configuration.methodRefExpressionDescription,
        v -> configuration.methodRefExpressionDescription = v,
        baseConfiguration.methodRefExpressionDescription);
    text(
        miscFields,
        () -> configuration.typeDescription,
        v -> configuration.typeDescription = v,
        baseConfiguration.typeDescription);
    text(
        miscFields,
        () -> configuration.diamondTypeDescription,
        v -> configuration.diamondTypeDescription = v,
        baseConfiguration.diamondTypeDescription);
    text(
        miscFields,
        () -> configuration.importStaticReferenceDescription,
        v -> configuration.importStaticReferenceDescription = v,
        baseConfiguration.importStaticReferenceDescription);
    text(
        miscFields,
        () -> configuration.providesWithListDescription,
        v -> configuration.providesWithListDescription = v,
        baseConfiguration.providesWithListDescription);
    text(
        miscFields,
        () -> configuration.opensStatementDescription,
        v -> configuration.opensStatementDescription = v,
        baseConfiguration.opensStatementDescription);
    text(
        miscFields,
        () -> configuration.exportsStatementDescription,
        v -> configuration.exportsStatementDescription = v,
        baseConfiguration.exportsStatementDescription);
    text(
        miscFields,
        () -> configuration.throwsListDescription,
        v -> configuration.throwsListDescription = v,
        baseConfiguration.throwsListDescription);
    text(
        miscFields,
        () -> configuration.extendsBoundListDescription,
        v -> configuration.extendsBoundListDescription = v,
        baseConfiguration.extendsBoundListDescription);
    text(
        miscFields,
        () -> configuration.implementsListDescription,
        v -> configuration.implementsListDescription = v,
        baseConfiguration.implementsListDescription);
    text(
        miscFields,
        () -> configuration.extendsListDescription,
        v -> configuration.extendsListDescription = v,
        baseConfiguration.extendsListDescription);
    text(
        miscFields,
        () -> configuration.emptyExpressionDescription,
        v -> configuration.emptyExpressionDescription = v,
        baseConfiguration.emptyExpressionDescription);
    text(
        miscFields,
        () -> configuration.switchExpressionDescription,
        v -> configuration.switchExpressionDescription = v,
        baseConfiguration.switchExpressionDescription);
    text(
        miscFields,
        () -> configuration.switchLabeledRuleDescription,
        v -> configuration.switchLabeledRuleDescription = v,
        baseConfiguration.switchLabeledRuleDescription);
    text(
        miscFields,
        () -> configuration.moduleReferenceDescription,
        v -> configuration.moduleReferenceDescription = v,
        baseConfiguration.moduleReferenceDescription);
    text(
        miscFields,
        () -> configuration.javaCodeReferenceDescription,
        v -> configuration.javaCodeReferenceDescription = v,
        baseConfiguration.javaCodeReferenceDescription);
    text(
        miscFields,
        () -> configuration.referenceExpressionDescription,
        v -> configuration.referenceExpressionDescription = v,
        baseConfiguration.referenceExpressionDescription);
    text(
        miscFields,
        () -> configuration.parenthExpressionDescription,
        v -> configuration.parenthExpressionDescription = v,
        baseConfiguration.parenthExpressionDescription);
    text(
        miscFields,
        () -> configuration.docMethodOrFieldRefDescription,
        v -> configuration.docMethodOrFieldRefDescription = v,
        baseConfiguration.docMethodOrFieldRefDescription);
    text(
        miscFields,
        () -> configuration.docParameterRefDescription,
        v -> configuration.docParameterRefDescription = v,
        baseConfiguration.docParameterRefDescription);
    text(
        miscFields,
        () -> configuration.docTagValueElementDescription,
        v -> configuration.docTagValueElementDescription = v,
        baseConfiguration.docTagValueElementDescription);
    text(
        miscFields,
        () -> configuration.docReferenceHolderDescription,
        v -> configuration.docReferenceHolderDescription = v,
        baseConfiguration.docReferenceHolderDescription);
    text(
        miscFields,
        () -> configuration.docTypeHolderDescription,
        v -> configuration.docTypeHolderDescription = v,
        baseConfiguration.docTypeHolderDescription);
    text(
        miscFields,
        () -> configuration.kotlinClassDescription,
        v -> configuration.kotlinClassDescription = v,
        baseConfiguration.kotlinClassDescription);
    text(
        miscFields,
        () -> configuration.kotlinFunctionDescription,
        v -> configuration.kotlinFunctionDescription = v,
        baseConfiguration.kotlinFunctionDescription);
    text(
        miscFields,
        () -> configuration.kotlinPropertyDescription,
        v -> configuration.kotlinPropertyDescription = v,
        baseConfiguration.kotlinPropertyDescription);
    text(
        miscFields,
        () -> configuration.kotlinIfExpressionDescription,
        v -> configuration.kotlinIfExpressionDescription = v,
        baseConfiguration.kotlinIfExpressionDescription);
    text(
        miscFields,
        () -> configuration.kotlinWhenExpressionDescription,
        v -> configuration.kotlinWhenExpressionDescription = v,
        baseConfiguration.kotlinWhenExpressionDescription);
    text(
        miscFields,
        () -> configuration.kotlinForLoopDescription,
        v -> configuration.kotlinForLoopDescription = v,
        baseConfiguration.kotlinForLoopDescription);
    text(
        miscFields,
        () -> configuration.kotlinWhileLoopDescription,
        v -> configuration.kotlinWhileLoopDescription = v,
        baseConfiguration.kotlinWhileLoopDescription);
    text(
        miscFields,
        () -> configuration.kotlinTryExpressionDescription,
        v -> configuration.kotlinTryExpressionDescription = v,
        baseConfiguration.kotlinTryExpressionDescription);
    text(
        miscFields,
        () -> configuration.kotlinLambdaExpressionDescription,
        v -> configuration.kotlinLambdaExpressionDescription = v,
        baseConfiguration.kotlinLambdaExpressionDescription);
    text(
        miscFields,
        () -> configuration.kotlinElvisExpressionDescription,
        v -> configuration.kotlinElvisExpressionDescription = v,
        baseConfiguration.kotlinElvisExpressionDescription);
  }

  private void checkBox(
      java.util.List<BeanField> fields,
      Supplier<Boolean> getter,
      Consumer<Boolean> setter,
      String title) {
    fields.add(new CheckBoxField(getter, setter, title));
  }

  private void text(
      java.util.List<BeanField> fields,
      Supplier<String> getter,
      Consumer<String> setter,
      String title) {
    fields.add(new TextField(getter, setter, title));
  }

  private void numeric(
      java.util.List<BeanField> fields,
      Supplier<Integer> getter,
      Consumer<Integer> setter,
      String title) {
    fields.add(new NumericField(getter, setter, title));
  }

  private void colorPicker(
      java.util.List<BeanField> fields,
      Supplier<Integer> getter,
      Consumer<Integer> setter,
      String title) {
    fields.add(new ColorPickerField(getter, setter, title));
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Code Metrics With Kotlin";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return "code.metrics";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    JTabbedPane tabbedPane = new JBTabbedPane();

    // Create Basics tab using FormBuilder for proper layout
    FormBuilder basicsBuilder = FormBuilder.createFormBuilder();
    basicsBuilder.setFormLeftIndent(12);

    // Color Configuration section
    basicsBuilder.addComponent(new TitledSeparator("Color Configuration"));
    addFieldsToBuilder(basicsBuilder, basicFields, 0, 4,
        "Configure the colors used for different complexity levels");

    // Complexity Thresholds section
    basicsBuilder.addSeparator(12);
    basicsBuilder.addComponent(new TitledSeparator("Complexity Thresholds"));
    addFieldsToBuilder(basicsBuilder, basicFields, 4, 8,
        "Define the complexity values that determine color coding");

    // Visibility Controls section
    basicsBuilder.addSeparator(12);
    basicsBuilder.addComponent(new TitledSeparator("Visibility Controls"));
    addFieldsToBuilder(basicsBuilder, basicFields, 8, 9,
        "Control when metrics are displayed");
    addFieldsToBuilder(basicsBuilder, basicFields, 9, 13,
        "Java language elements");
    addFieldsToBuilder(basicsBuilder, basicFields, 13, basicFields.size(),
        "Kotlin language elements");

    JPanel basicsPanel = basicsBuilder
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();

    // Wrap in scroll pane for independent scrolling (Configurable.NoScroll removes outer scroll)
    JBScrollPane basicsScroll = new JBScrollPane(basicsPanel);
    basicsScroll.setBorder(JBUI.Borders.empty());
    basicsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // Create Advanced tab using FormBuilder for proper layout
    FormBuilder advancedBuilder = FormBuilder.createFormBuilder();
    advancedBuilder.setFormLeftIndent(12);

    // Complexity Templates section
    advancedBuilder.addComponent(new TitledSeparator("Complexity Templates"));
    addFieldsToBuilder(advancedBuilder, advancedFields, 0, 5,
        "Customize the text displayed for complexity hints");

    // Element Complexity Weights section
    advancedBuilder.addSeparator(12);
    advancedBuilder.addComponent(new TitledSeparator("Element Complexity Weights"));
    addFieldsToBuilder(advancedBuilder, advancedFields, 5, advancedFields.size(),
        "Configure complexity values for individual PSI elements");

    JPanel advancedPanel = advancedBuilder
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();

    // Wrap in scroll pane for independent scrolling (Configurable.NoScroll removes outer scroll)
    JBScrollPane advancedScroll = new JBScrollPane(advancedPanel);
    advancedScroll.setBorder(JBUI.Borders.empty());
    advancedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // Create Miscellaneous tab using FormBuilder for proper layout
    FormBuilder miscBuilder = FormBuilder.createFormBuilder();
    miscBuilder.setFormLeftIndent(12);

    // Element Descriptions section
    miscBuilder.addComponent(new TitledSeparator("Element Descriptions"));
    addFieldsToBuilder(miscBuilder, miscFields, 0, miscFields.size(),
        "Customize the description text for individual PSI elements");

    JPanel miscPanel = miscBuilder
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();

    // Wrap in scroll pane for independent scrolling (Configurable.NoScroll removes outer scroll)
    JBScrollPane miscScroll = new JBScrollPane(miscPanel);
    miscScroll.setBorder(JBUI.Borders.empty());
    miscScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // Add tabs with their own scroll panes (per-tab scrolling)
    tabbedPane.add("Basics", basicsScroll);
    tabbedPane.add("Advanced", advancedScroll);
    tabbedPane.add("Miscellaneous", miscScroll);

    // Create main container with Reset to Defaults button
    JPanel mainContainer = new JPanel(new BorderLayout());
    mainContainer.add(tabbedPane, BorderLayout.CENTER);

    // Create button panel at the bottom
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setBorder(JBUI.Borders.empty(8, 12, 8, 12));

    JButton resetButton = new JButton("Reset to Defaults");
    resetButton.addActionListener(e -> resetToDefaults());
    buttonPanel.add(resetButton);

    mainContainer.add(buttonPanel, BorderLayout.SOUTH);

    return mainContainer;
  }

  private void resetToDefaults() {
    // Show confirmation dialog
    int result = javax.swing.JOptionPane.showConfirmDialog(
        null,
        "Are you sure you want to reset all settings to defaults? This cannot be undone.",
        "Reset to Defaults",
        javax.swing.JOptionPane.YES_NO_OPTION,
        javax.swing.JOptionPane.WARNING_MESSAGE);

    if (result == javax.swing.JOptionPane.YES_OPTION) {
      // Create a new instance to get default values
      MetricsConfiguration defaults = new MetricsConfiguration();

      // Copy all default values to current configuration
      configuration.complexityColorLow = defaults.complexityColorLow;
      configuration.complexityColorNormal = defaults.complexityColorNormal;
      configuration.complexityColorHigh = defaults.complexityColorHigh;
      configuration.complexityColorExtreme = defaults.complexityColorExtreme;

      configuration.complexityLevelLow = defaults.complexityLevelLow;
      configuration.complexityLevelNormal = defaults.complexityLevelNormal;
      configuration.complexityLevelHigh = defaults.complexityLevelHigh;
      configuration.complexityLevelExtreme = defaults.complexityLevelExtreme;

      configuration.hiddenUnder = defaults.hiddenUnder;

      configuration.metricsForAnonymousClass = defaults.metricsForAnonymousClass;
      configuration.metricsForAClass = defaults.metricsForAClass;
      configuration.metricsForMethod = defaults.metricsForMethod;
      configuration.metricsForLambdaExpression = defaults.metricsForLambdaExpression;

      configuration.metricsForKotlinClass = defaults.metricsForKotlinClass;
      configuration.metricsForKotlinFunction = defaults.metricsForKotlinFunction;
      configuration.metricsForKotlinProperty = defaults.metricsForKotlinProperty;
      configuration.metricsForKotlinLambda = defaults.metricsForKotlinLambda;
      configuration.metricsForKotlinIf = defaults.metricsForKotlinIf;
      configuration.metricsForKotlinWhen = defaults.metricsForKotlinWhen;
      configuration.metricsForKotlinFor = defaults.metricsForKotlinFor;
      configuration.metricsForKotlinWhile = defaults.metricsForKotlinWhile;
      configuration.metricsForKotlinTry = defaults.metricsForKotlinTry;

      configuration.complexityLevelExtremeDescription = defaults.complexityLevelExtremeDescription;
      configuration.complexityLevelHighDescription = defaults.complexityLevelHighDescription;
      configuration.complexityLevelNormalDescription = defaults.complexityLevelNormalDescription;
      configuration.complexityLevelLowDescription = defaults.complexityLevelLowDescription;
      configuration.complexityTemplate = defaults.complexityTemplate;

      configuration.anonymousClass = defaults.anonymousClass;
      configuration.arrayAccessExpression = defaults.arrayAccessExpression;
      configuration.arrayInitializerExpression = defaults.arrayInitializerExpression;
      configuration.assertStatement = defaults.assertStatement;
      configuration.assignmentExpression = defaults.assignmentExpression;
      configuration.binaryExpression = defaults.binaryExpression;
      configuration.blockStatement = defaults.blockStatement;
      configuration.breakStatement = defaults.breakStatement;
      configuration.aClass = defaults.aClass;
      configuration.classInitializer = defaults.classInitializer;
      configuration.classObjectAccessExpression = defaults.classObjectAccessExpression;
      configuration.codeBlock = defaults.codeBlock;
      configuration.conditionalExpression = defaults.conditionalExpression;
      configuration.continueStatement = defaults.continueStatement;
      configuration.declarationStatement = defaults.declarationStatement;
      configuration.docComment = defaults.docComment;
      configuration.docTag = defaults.docTag;
      configuration.doWhileStatement = defaults.doWhileStatement;
      configuration.emptyStatement = defaults.emptyStatement;
      configuration.expressionList = defaults.expressionList;
      configuration.expressionListStatement = defaults.expressionListStatement;
      configuration.expressionStatement = defaults.expressionStatement;
      configuration.field = defaults.field;
      configuration.forStatement = defaults.forStatement;
      configuration.foreachStatement = defaults.foreachStatement;
      configuration.ifStatement = defaults.ifStatement;
      configuration.importList = defaults.importList;
      configuration.importStatement = defaults.importStatement;
      configuration.importStaticStatement = defaults.importStaticStatement;
      configuration.inlineDocTag = defaults.inlineDocTag;
      configuration.instanceOfExpression = defaults.instanceOfExpression;
      configuration.labeledStatement = defaults.labeledStatement;
      configuration.literalExpression = defaults.literalExpression;
      configuration.localVariable = defaults.localVariable;
      configuration.method = defaults.method;
      configuration.methodCallExpression = defaults.methodCallExpression;
      configuration.modifierList = defaults.modifierList;
      configuration.newExpression = defaults.newExpression;
      configuration.packageStatement = defaults.packageStatement;
      configuration.parameter = defaults.parameter;
      configuration.receiverParameter = defaults.receiverParameter;
      configuration.parameterList = defaults.parameterList;
      configuration.postfixExpression = defaults.postfixExpression;
      configuration.prefixExpression = defaults.prefixExpression;
      configuration.referenceParameterList = defaults.referenceParameterList;
      configuration.typeParameterList = defaults.typeParameterList;
      configuration.returnStatement = defaults.returnStatement;
      configuration.superExpression = defaults.superExpression;
      configuration.switchLabelStatement = defaults.switchLabelStatement;
      configuration.switchStatement = defaults.switchStatement;
      configuration.synchronizedStatement = defaults.synchronizedStatement;
      configuration.thisExpression = defaults.thisExpression;
      configuration.throwStatement = defaults.throwStatement;
      configuration.tryStatement = defaults.tryStatement;
      configuration.catchSection = defaults.catchSection;
      configuration.resourceList = defaults.resourceList;
      configuration.resourceVariable = defaults.resourceVariable;
      configuration.resourceExpression = defaults.resourceExpression;
      configuration.typeCastExpression = defaults.typeCastExpression;
      configuration.whileStatement = defaults.whileStatement;
      configuration.typeParameter = defaults.typeParameter;
      configuration.annotation = defaults.annotation;
      configuration.annotationParameterList = defaults.annotationParameterList;
      configuration.annotationArrayInitializer = defaults.annotationArrayInitializer;
      configuration.nameValuePair = defaults.nameValuePair;
      configuration.annotationMethod = defaults.annotationMethod;
      configuration.enumConstant = defaults.enumConstant;
      configuration.enumConstantInitializer = defaults.enumConstantInitializer;
      configuration.polyadicExpression = defaults.polyadicExpression;
      configuration.lambdaExpression = defaults.lambdaExpression;
      configuration.module = defaults.module;
      configuration.requiresStatement = defaults.requiresStatement;
      configuration.usesStatement = defaults.usesStatement;
      configuration.providesStatement = defaults.providesStatement;
      configuration.methodRefExpression = defaults.methodRefExpression;
      configuration.type = defaults.type;
      configuration.diamondType = defaults.diamondType;
      configuration.importStaticReference = defaults.importStaticReference;
      configuration.providesWithList = defaults.providesWithList;
      configuration.opensStatement = defaults.opensStatement;
      configuration.exportsStatement = defaults.exportsStatement;
      configuration.throwsList = defaults.throwsList;
      configuration.extendsBoundList = defaults.extendsBoundList;
      configuration.implementsList = defaults.implementsList;
      configuration.extendsList = defaults.extendsList;
      configuration.emptyExpression = defaults.emptyExpression;
      configuration.switchExpression = defaults.switchExpression;
      configuration.switchLabeledRule = defaults.switchLabeledRule;
      configuration.moduleReference = defaults.moduleReference;
      configuration.javaCodeReference = defaults.javaCodeReference;
      configuration.referenceExpression = defaults.referenceExpression;
      configuration.parenthExpression = defaults.parenthExpression;
      configuration.docMethodOrFieldRef = defaults.docMethodOrFieldRef;
      configuration.docParameterRef = defaults.docParameterRef;
      configuration.docTagValueElement = defaults.docTagValueElement;
      configuration.docReferenceHolder = defaults.docReferenceHolder;
      configuration.docTypeHolder = defaults.docTypeHolder;

      configuration.kotlinClass = defaults.kotlinClass;
      configuration.kotlinFunction = defaults.kotlinFunction;
      configuration.kotlinProperty = defaults.kotlinProperty;
      configuration.kotlinIfExpression = defaults.kotlinIfExpression;
      configuration.kotlinWhenExpression = defaults.kotlinWhenExpression;
      configuration.kotlinForLoop = defaults.kotlinForLoop;
      configuration.kotlinWhileLoop = defaults.kotlinWhileLoop;
      configuration.kotlinTryExpression = defaults.kotlinTryExpression;
      configuration.kotlinLambdaExpression = defaults.kotlinLambdaExpression;
      configuration.kotlinElvisExpression = defaults.kotlinElvisExpression;

      configuration.anonymousClassDescription = defaults.anonymousClassDescription;
      configuration.arrayAccessExpressionDescription = defaults.arrayAccessExpressionDescription;
      configuration.arrayInitializerExpressionDescription = defaults.arrayInitializerExpressionDescription;
      configuration.assertStatementDescription = defaults.assertStatementDescription;
      configuration.assignmentExpressionDescription = defaults.assignmentExpressionDescription;
      configuration.binaryExpressionDescription = defaults.binaryExpressionDescription;
      configuration.blockStatementDescription = defaults.blockStatementDescription;
      configuration.breakStatementDescription = defaults.breakStatementDescription;
      configuration.aClassDescription = defaults.aClassDescription;
      configuration.classInitializerDescription = defaults.classInitializerDescription;
      configuration.classObjectAccessExpressionDescription = defaults.classObjectAccessExpressionDescription;
      configuration.codeBlockDescription = defaults.codeBlockDescription;
      configuration.conditionalExpressionDescription = defaults.conditionalExpressionDescription;
      configuration.continueStatementDescription = defaults.continueStatementDescription;
      configuration.declarationStatementDescription = defaults.declarationStatementDescription;
      configuration.docCommentDescription = defaults.docCommentDescription;
      configuration.docTagDescription = defaults.docTagDescription;
      configuration.doWhileStatementDescription = defaults.doWhileStatementDescription;
      configuration.emptyStatementDescription = defaults.emptyStatementDescription;
      configuration.expressionListDescription = defaults.expressionListDescription;
      configuration.expressionListStatementDescription = defaults.expressionListStatementDescription;
      configuration.expressionStatementDescription = defaults.expressionStatementDescription;
      configuration.fieldDescription = defaults.fieldDescription;
      configuration.forStatementDescription = defaults.forStatementDescription;
      configuration.foreachStatementDescription = defaults.foreachStatementDescription;
      configuration.ifStatementDescription = defaults.ifStatementDescription;
      configuration.importListDescription = defaults.importListDescription;
      configuration.importStatementDescription = defaults.importStatementDescription;
      configuration.importStaticStatementDescription = defaults.importStaticStatementDescription;
      configuration.inlineDocTagDescription = defaults.inlineDocTagDescription;
      configuration.instanceOfExpressionDescription = defaults.instanceOfExpressionDescription;
      configuration.labeledStatementDescription = defaults.labeledStatementDescription;
      configuration.literalExpressionDescription = defaults.literalExpressionDescription;
      configuration.localVariableDescription = defaults.localVariableDescription;
      configuration.methodDescription = defaults.methodDescription;
      configuration.methodCallExpressionDescription = defaults.methodCallExpressionDescription;
      configuration.modifierListDescription = defaults.modifierListDescription;
      configuration.newExpressionDescription = defaults.newExpressionDescription;
      configuration.packageStatementDescription = defaults.packageStatementDescription;
      configuration.parameterDescription = defaults.parameterDescription;
      configuration.receiverParameterDescription = defaults.receiverParameterDescription;
      configuration.parameterListDescription = defaults.parameterListDescription;
      configuration.postfixExpressionDescription = defaults.postfixExpressionDescription;
      configuration.prefixExpressionDescription = defaults.prefixExpressionDescription;
      configuration.referenceParameterListDescription = defaults.referenceParameterListDescription;
      configuration.typeParameterListDescription = defaults.typeParameterListDescription;
      configuration.returnStatementDescription = defaults.returnStatementDescription;
      configuration.superExpressionDescription = defaults.superExpressionDescription;
      configuration.switchLabelStatementDescription = defaults.switchLabelStatementDescription;
      configuration.switchStatementDescription = defaults.switchStatementDescription;
      configuration.synchronizedStatementDescription = defaults.synchronizedStatementDescription;
      configuration.thisExpressionDescription = defaults.thisExpressionDescription;
      configuration.throwStatementDescription = defaults.throwStatementDescription;
      configuration.tryStatementDescription = defaults.tryStatementDescription;
      configuration.catchSectionDescription = defaults.catchSectionDescription;
      configuration.resourceListDescription = defaults.resourceListDescription;
      configuration.resourceVariableDescription = defaults.resourceVariableDescription;
      configuration.resourceExpressionDescription = defaults.resourceExpressionDescription;
      configuration.typeCastExpressionDescription = defaults.typeCastExpressionDescription;
      configuration.whileStatementDescription = defaults.whileStatementDescription;
      configuration.typeParameterDescription = defaults.typeParameterDescription;
      configuration.annotationDescription = defaults.annotationDescription;
      configuration.annotationParameterListDescription = defaults.annotationParameterListDescription;
      configuration.annotationArrayInitializerDescription = defaults.annotationArrayInitializerDescription;
      configuration.nameValuePairDescription = defaults.nameValuePairDescription;
      configuration.annotationMethodDescription = defaults.annotationMethodDescription;
      configuration.enumConstantDescription = defaults.enumConstantDescription;
      configuration.enumConstantInitializerDescription = defaults.enumConstantInitializerDescription;
      configuration.polyadicExpressionDescription = defaults.polyadicExpressionDescription;
      configuration.lambdaExpressionDescription = defaults.lambdaExpressionDescription;
      configuration.moduleDescription = defaults.moduleDescription;
      configuration.requiresStatementDescription = defaults.requiresStatementDescription;
      configuration.usesStatementDescription = defaults.usesStatementDescription;
      configuration.providesStatementDescription = defaults.providesStatementDescription;
      configuration.methodRefExpressionDescription = defaults.methodRefExpressionDescription;
      configuration.typeDescription = defaults.typeDescription;
      configuration.diamondTypeDescription = defaults.diamondTypeDescription;
      configuration.importStaticReferenceDescription = defaults.importStaticReferenceDescription;
      configuration.providesWithListDescription = defaults.providesWithListDescription;
      configuration.opensStatementDescription = defaults.opensStatementDescription;
      configuration.exportsStatementDescription = defaults.exportsStatementDescription;
      configuration.throwsListDescription = defaults.throwsListDescription;
      configuration.extendsBoundListDescription = defaults.extendsBoundListDescription;
      configuration.implementsListDescription = defaults.implementsListDescription;
      configuration.extendsListDescription = defaults.extendsListDescription;
      configuration.emptyExpressionDescription = defaults.emptyExpressionDescription;
      configuration.switchExpressionDescription = defaults.switchExpressionDescription;
      configuration.switchLabeledRuleDescription = defaults.switchLabeledRuleDescription;
      configuration.moduleReferenceDescription = defaults.moduleReferenceDescription;
      configuration.javaCodeReferenceDescription = defaults.javaCodeReferenceDescription;
      configuration.referenceExpressionDescription = defaults.referenceExpressionDescription;
      configuration.parenthExpressionDescription = defaults.parenthExpressionDescription;
      configuration.docMethodOrFieldRefDescription = defaults.docMethodOrFieldRefDescription;
      configuration.docParameterRefDescription = defaults.docParameterRefDescription;
      configuration.docTagValueElementDescription = defaults.docTagValueElementDescription;
      configuration.docReferenceHolderDescription = defaults.docReferenceHolderDescription;
      configuration.docTypeHolderDescription = defaults.docTypeHolderDescription;
      configuration.kotlinClassDescription = defaults.kotlinClassDescription;
      configuration.kotlinFunctionDescription = defaults.kotlinFunctionDescription;
      configuration.kotlinPropertyDescription = defaults.kotlinPropertyDescription;
      configuration.kotlinIfExpressionDescription = defaults.kotlinIfExpressionDescription;
      configuration.kotlinWhenExpressionDescription = defaults.kotlinWhenExpressionDescription;
      configuration.kotlinForLoopDescription = defaults.kotlinForLoopDescription;
      configuration.kotlinWhileLoopDescription = defaults.kotlinWhileLoopDescription;
      configuration.kotlinTryExpressionDescription = defaults.kotlinTryExpressionDescription;
      configuration.kotlinLambdaExpressionDescription = defaults.kotlinLambdaExpressionDescription;
      configuration.kotlinElvisExpressionDescription = defaults.kotlinElvisExpressionDescription;

      // Refresh all UI fields to show the new values
      basicFields.forEach(BeanField::reset);
      advancedFields.forEach(BeanField::reset);
      miscFields.forEach(BeanField::reset);

      // Notify listeners to update the editor
      configuration.notifyListeners();
    }
  }

  private void addFieldsToBuilder(FormBuilder builder, java.util.List<BeanField> fields,
                                    int start, int end, String helpText) {
    if (helpText != null && !helpText.isEmpty()) {
      JBLabel help = new JBLabel(helpText);
      help.setForeground(JBColor.namedColor("Label.infoForeground",
          new JBColor(0x808080, 0x8C8C8C)));
      help.setFont(help.getFont().deriveFont(11f));
      help.setBorder(JBUI.Borders.emptyLeft(8));
      builder.addComponent(help);
    }

    for (int i = start; i < Math.min(end, fields.size()); i++) {
      builder.addComponent(fields.get(i).getComponent());
    }
  }

  public boolean isModified() {
    return basicFields.stream().anyMatch(BeanField::isModified)
        || advancedFields.stream().anyMatch(BeanField::isModified)
        || miscFields.stream().anyMatch(BeanField::isModified);
  }

  public void apply() {
    // Validate all numeric fields before applying
    java.util.List<String> invalidFields = new java.util.ArrayList<>();

    for (BeanField field : basicFields) {
      if (field instanceof NumericField) {
        NumericField numField = (NumericField) field;
        if (!numField.isValidInput()) {
          invalidFields.add(numField.title);
        }
      }
    }
    for (BeanField field : advancedFields) {
      if (field instanceof NumericField) {
        NumericField numField = (NumericField) field;
        if (!numField.isValidInput()) {
          invalidFields.add(numField.title);
        }
      }
    }
    for (BeanField field : miscFields) {
      if (field instanceof NumericField) {
        NumericField numField = (NumericField) field;
        if (!numField.isValidInput()) {
          invalidFields.add(numField.title);
        }
      }
    }

    // Show error if there are invalid fields
    if (!invalidFields.isEmpty()) {
      String fieldList = String.join("\n  - ", invalidFields);
      javax.swing.SwingUtilities.invokeLater(() -> {
        javax.swing.JOptionPane.showMessageDialog(
            null,
            "Please enter valid integer values for the following fields:\n  - " + fieldList,
            "Invalid Input",
            javax.swing.JOptionPane.ERROR_MESSAGE);
      });
      return; // Don't save invalid configuration
    }

    try {
      // Apply all fields
      for (BeanField field : basicFields) {
        field.apply();
      }
      for (BeanField field : advancedFields) {
        field.apply();
      }
      for (BeanField field : miscFields) {
        field.apply();
      }

      // Validate and auto-correct configuration
      boolean wasModified = configuration.validateAndFixState();

      // Update UI to reflect auto-corrected values
      basicFields.forEach(BeanField::reset);
      advancedFields.forEach(BeanField::reset);
      miscFields.forEach(BeanField::reset);

      // Notify user if values were auto-corrected
      if (wasModified) {
        javax.swing.SwingUtilities.invokeLater(() -> {
          javax.swing.JOptionPane.showMessageDialog(
              null,
              "Configuration values were automatically adjusted to ensure valid thresholds.\nPlease review the updated values.",
              "Settings Auto-Corrected",
              javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });
      }

      configuration.notifyListeners();
    } catch (IllegalStateException e) {
      // Configuration validation failed - show error to user
      javax.swing.SwingUtilities.invokeLater(() -> {
        javax.swing.JOptionPane.showMessageDialog(
            null,
            e.getMessage(),
            "Invalid Configuration",
            javax.swing.JOptionPane.ERROR_MESSAGE);
      });
      // Don't save invalid configuration
      reset();
    }
  }

  public void reset() {
    basicFields.forEach(BeanField::reset);
    advancedFields.forEach(BeanField::reset);
    miscFields.forEach(BeanField::reset);
  }

  @Override
  public void disposeUIResources() {}

  private abstract static class BeanField<PROPTYPE, T extends JComponent> {
    private Supplier<PROPTYPE> getter;
    private Consumer<PROPTYPE> setter;

    T myComponent;

    private BeanField(Supplier<PROPTYPE> getter, Consumer<PROPTYPE> setter) {
      this.getter = getter;
      this.setter = setter;
    }

    T getComponent() {
      if (myComponent == null) {
        myComponent = createComponent();
      }
      return myComponent;
    }

    abstract T createComponent();

    boolean isModified() {
      final Object componentValue = getComponentValue();
      final Object beanValue = getBeanValue();
      return !Comparing.equal(componentValue, beanValue);
    }

    void apply() {
      setBeanValue(getComponentValue());
    }

    void reset() {
      setComponentValue(getBeanValue());
    }

    abstract PROPTYPE getComponentValue();

    abstract void setComponentValue(PROPTYPE instance);

    PROPTYPE getBeanValue() {
      return getter.get();
    }

    void setBeanValue(PROPTYPE value) {
      setter.accept(value);
    }
  }

  private static class ColorPickerField extends BeanField<Integer, JPanel> {

    private String title;
    private ColorPanel colorPanel;

    private ColorPickerField(
        Supplier<Integer> getter, Consumer<Integer> setter, final String title) {
      super(getter, setter);
      this.title = title;
      colorPanel = new ColorPanel();
    }

    JPanel createComponent() {
      JPanel jPanel = new JPanel();
      jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
      jPanel.setBorder(JBUI.Borders.empty(4, 8, 4, 8));

      JLabel label = new JLabel(title);
      label.setPreferredSize(new Dimension(300, 25));
      label.setMinimumSize(new Dimension(300, 25));
      label.setMaximumSize(new Dimension(300, 25));

      jPanel.add(label);
      jPanel.add(Box.createHorizontalStrut(12));
      jPanel.add(colorPanel);
      jPanel.add(Box.createHorizontalGlue());

      jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
      jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      return jPanel;
    }

    Integer getComponentValue() {
      return colorPanel.getSelectedColor().getRGB();
    }

    void setComponentValue(final Integer instance) {
      Color color = new Color(instance, true);
      colorPanel.setSelectedColor(color);
    }
  }

  private class NumericField extends BeanField<Integer, JPanel> {

    private JBTextField jbTextField;
    private String title;

    public NumericField(Supplier<Integer> getter, Consumer<Integer> setter, String title) {
      super(getter, setter);
      this.title = title;
      jbTextField = new JBTextField();

      // Add real-time validation feedback
      jbTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          validateInput();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          validateInput();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          validateInput();
        }

        private void validateInput() {
          if (isValidInput()) {
            jbTextField.setToolTipText(null);
            jbTextField.putClientProperty("JComponent.outline", null);
          } else {
            jbTextField.setToolTipText("Please enter a valid integer");
            jbTextField.putClientProperty("JComponent.outline", "error");
          }
        }
      });
    }

    @Override
    JPanel createComponent() {
      JPanel jPanel = new JPanel();
      jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
      jPanel.setBorder(JBUI.Borders.empty(4, 8, 4, 8));

      JLabel label = new JLabel(title);
      label.setPreferredSize(new Dimension(300, 25));
      label.setMinimumSize(new Dimension(300, 25));
      label.setMaximumSize(new Dimension(300, 25));

      jbTextField.setPreferredSize(new Dimension(100, 25));
      jbTextField.setMaximumSize(new Dimension(100, 25));

      jPanel.add(label);
      jPanel.add(Box.createHorizontalStrut(12));
      jPanel.add(jbTextField);
      jPanel.add(Box.createHorizontalGlue());

      jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
      jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      return jPanel;
    }

    @Override
    Integer getComponentValue() {
      String text = jbTextField.getText();
      try {
        return Integer.parseInt(text);
      } catch (NumberFormatException e) {
        // Invalid input - return current bean value to prevent modification
        // This allows isModified() to work without throwing exceptions
        return getBeanValue();
      }
    }

    boolean isValidInput() {
      String text = jbTextField.getText();
      try {
        Integer.parseInt(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }

    @Override
    void setComponentValue(Integer instance) {
      jbTextField.setText(Objects.toString(instance, "0"));
    }
  }

  private class TextField extends BeanField<String, JPanel> {

    private JBTextField jbTextField;
    private String title;

    public TextField(Supplier<String> getter, Consumer<String> setter, String title) {
      super(getter, setter);
      this.title = title;
      jbTextField = new JBTextField();
    }

    @Override
    JPanel createComponent() {
      JPanel jPanel = new JPanel();
      jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
      jPanel.setBorder(JBUI.Borders.empty(4, 8, 4, 8));

      JLabel label = new JLabel(title);
      label.setPreferredSize(new Dimension(300, 25));
      label.setMinimumSize(new Dimension(300, 25));
      label.setMaximumSize(new Dimension(300, 25));

      jbTextField.setPreferredSize(new Dimension(200, 25));
      jbTextField.setMaximumSize(new Dimension(400, 25));

      jPanel.add(label);
      jPanel.add(Box.createHorizontalStrut(12));
      jPanel.add(jbTextField);
      jPanel.add(Box.createHorizontalGlue());

      jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
      jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      return jPanel;
    }

    @Override
    String getComponentValue() {
      return jbTextField.getText();
    }

    @Override
    void setComponentValue(String instance) {
      jbTextField.setText(instance);
    }
  }

  private class CheckBoxField extends BeanField<Boolean, JPanel> {

    private JBCheckBox jbCheckBox;
    private String title;

    public CheckBoxField(Supplier<Boolean> getter, Consumer<Boolean> setter, String title) {
      super(getter, setter);
      this.title = title;
      jbCheckBox = new JBCheckBox();
    }

    @Override
    JPanel createComponent() {
      JPanel jPanel = new JPanel();
      jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
      jPanel.setBorder(JBUI.Borders.empty(4, 8, 4, 8));

      jbCheckBox.setPreferredSize(new Dimension(20, 25));
      jbCheckBox.setMaximumSize(new Dimension(20, 25));

      JLabel label = new JLabel(title);

      jPanel.add(jbCheckBox);
      jPanel.add(Box.createHorizontalStrut(12));
      jPanel.add(label);
      jPanel.add(Box.createHorizontalGlue());

      jPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
      jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      return jPanel;
    }

    @Override
    Boolean getComponentValue() {
      return jbCheckBox.isSelected();
    }

    @Override
    void setComponentValue(Boolean instance) {
      jbCheckBox.setSelected(Boolean.TRUE.equals(instance));
    }
  }
}
