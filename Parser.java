//  ************** REQUIRES JAVA 17 OR ABOVE! (https://adoptium.net/) ************** //
package compiler;

import java.util.logging.Logger;

/*
 * GRAMMAR FOR PROCESSING SIMPLE SENTENCES:
 *
 * <SENTENCE> ::= <NOUN_PHRASE> <VERB_PHRASE> <NOUN_PHRASE> <PREP_PHRASE> <SENTENCE_TAIL> $$
 * <SENTENCE_TAIL> ::= <CONJ> <SENTENCE> | <EOS>
 *
 * <NOUN_PHRASE> ::= <ART> <ADJ_LIST> <NOUN>
 * <ADJ_LIST> ::= <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 * <ADJ_TAIL> ::= <COMMA> <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 *
 * <VERB_PHRASE> ::= <ADVERB> <VERB> | <VERB>
 * <PREP_PHRASE> ::= <PREPOSITION> <NOUN_PHRASE> | <<EMPTY>>
 *
 * // *** Terminal Productions (Actual terminals omitted, but they are just the
 * valid words in the language). ***
 *
 * <COMMA> ::= ','
 * <EOS> ::= '.' | '!'
 *
 * <ADJECTIVE> ::= ...adjective list...
 * <ADVERB> ::= ...adverb list...
 * <ART> ::= ...article list...
 * <CONJ> ::= ...conjunction list...
 * <NOUN> ::= ...noun list...
 * <PREPOSITION> ::= ...preposition list...
 * <VERB> ::= ...verb list....
 */

/**
 * The Syntax Analyzer.
 * <p>
 * ************** NOTE: REQUIRES JAVA 11 OR ABOVE! ******************
 */
public class Parser {

    // The lexer which will provide the tokens
    private final LexicalAnalyzer lexer;

    // The actual "code generator"
    private final CodeGenerator codeGenerator;

    /**
     * This is the constructor for the Parser class which
     * accepts a LexicalAnalyzer and a CodeGenerator object as parameters.
     *
     * @param lexer         The Lexer Object
     * @param codeGenerator The CodeGenerator Object
     */
    public Parser(LexicalAnalyzer lexer, CodeGenerator codeGenerator) {
        this.lexer = lexer;
        this.codeGenerator = codeGenerator;

        // Change this to automatically prompt to see the Open WebGraphViz dialog or not.
        MAIN.PROMPT_FOR_GRAPHVIZ = true;
    }

    /*
     * Since the "Compiler" portion of the code knows nothing about the start rule,
     * the "analyze" method must invoke the start rule.
     *
     * Begin analyzing...
     */
    void analyze() {
        try {
            // Generate header for our output
            TreeNode startNode = codeGenerator.writeHeader("PARSE TREE");

            // THIS IS OUR START RULE
            this.beginParsing(startNode);

            // generate footer for our output
            codeGenerator.writeFooter();

        } catch (ParseException ex) {
            final String msg = String.format("%s\n", ex.getMessage());
            Logger.getAnonymousLogger().severe(msg);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is just an intermediate method to make it easy to change the start rule of the grammar.
     *
     * @param parentNode The parent node for the parse tree
     * @throws ParseException If there is a syntax error
     */
    private void beginParsing(final TreeNode parentNode) throws ParseException {
        // Invoke the start rule.
        // TODO: Change if necessary!
        this.SENTENCE(parentNode);
    }

    // <PROGRAM> ::= <STMT_LIST> $$
    private void PROGRAM(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
            this.MATCH(thisNode, Token.STMT_LIST);
        // Test for the end of input.
        if (lexer.currentToken() != Token.$$) {
            this.raiseException(Token.$$, thisNode);
        }
    }

    // <STMT_LIST> ::= <STMT> <STMT_LIST> | <EMPTY>
    private void STMT_LIST(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        if (lexer.currentToken() == Token.STMT) {
            this.MATCH(thisNode, Token.STMT);
            this.STMT_LIST(thisNode)
        }else{
            this.EMPTY(thisNode);
        }
    }


    //######## Confused ############
    //###############################
    // <STMT> ::= <ID> := <EXPR> | read <ID> | write <EXPR>
    private void STMT(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        if (lexer.currentToken() == Token.ID) {
            this.MATCH(thisNode, Token.STMT);

        }else{
            this.EMPTY(thisNode);
        }
    }

    // <EXPR> ::= <TERM> <TERM_TAIL> 
    private void EXPR(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.MATCH(thisNode, Token.TERM);
        this.MATCH(thisNode, Token.TERM_TAIL);
    }

    // <TERM_TAIL> ::= <ADD_OP> <TERM> <TERM_TAIL> | <EMPTY> 
    private void TERM_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        
        if(lexer.currentToken() == Token.ADD_OP){
            this.MATCH(thisNode, Token.ADD_OP);
            this.MATCH(thisNode, Token.TERM);
            this.MATCH(thisNode, Token.TERM_TAIL);
        }else{
            this.EMPTY(thisNode);
        }
    }

    // <TERM> ::= <FACTOR> <FACTOR_TAIL> 
    private void TERM(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.MATCH(thisNode, Token.FACTOR);
        this.MATCH(thisNode, Token.FACTOR_TAIL);
    }

    // <FACTOR_TAIL> ::= <MULT_OP> <FACTOR> <FACTOR_TAIL> | <EMPTY> 
    private void FACTOR_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        
        if(lexer.currentToken() == Token.MULT_OP){
            this.MATCH(thisNode, Token.MULT_OP);
            this.MATCH(thisNode, Token.FACTOR);
            this.MATCH(thisNode, Token.FACTOR_TAIL);
        }else{
            this.EMPTY(thisNode);
        }
    }

    //####### Confused ###############
    //##############################

    // <FACTOR> ::= ( <EXPR> ) | <ID> | <NUMBER>
    private void TERM(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        
        // if(lexer.currentToken() == )
        // this.MATCH(thisNode, Token.FACTOR);
        // this.MATCH(thisNode, Token.FACTOR_TAIL);
    }
    // <CONDITION> ::= <EXPR> <RELATION> <EXPR>
    private void CONDITION(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.MATCH(thisNode, Token.EXPR);
        this.MATCH(thisNode, Token.RELATION);
        this.MATCH(thisNode, Token.EXPR);
    }

    // <WHILE_STATEMENT> ::= <CONDITION> <DO> <OD>
    // <DO> ::= "DO" <STMT_LIST> <DO_TAIL>
    // <DO_TAIL> ::= <UNTIL> | <EMPYY>
    // <UNTIL> ::= "UNTIL" <CONDITION>

    //<IF> ::= "IF" <CONDITION> <THEN> <FI>
    //<THEN> ::= "THEN" <STMT_LIST> <ELSE>
    //<ELSE> ::= ("ELSE" <STMT_LIST>)| <EMPTY> 













    // <SENTENCE> ::= <NOUN_PHRASE> <VERB_PHRASE> <NOUN_PHRASE> <PREP_PHRASE> <SENTENCE_TAIL> $$
    private void SENTENCE(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        this.NOUN_PHRASE(thisNode);
        this.VERB_PHRASE(thisNode);
        this.NOUN_PHRASE(thisNode);
        this.PREP_PHRASE(thisNode);
        this.SENTENCE_TAIL(thisNode);

        // Test for the end of input.
        if (lexer.currentToken() != Token.$$) {
            this.raiseException(Token.$$, thisNode);
        }
    }

    // <SENTENCE_TAIL> ::= <CONJ> <SENTENCE> | <EOS>
    private void SENTENCE_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.CONJUNCTION) {
            this.MATCH(thisNode, Token.CONJUNCTION);
            this.SENTENCE(thisNode);
        } else {
            this.MATCH(thisNode, Token.PERIOD);
        }
    }

    // <NOUN_PHRASE> ::= <ART> <ADJ_LIST> <NOUN>
    private void NOUN_PHRASE(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        this.MATCH(thisNode, Token.ARTICLE);
        this.ADJ_LIST(thisNode);
        this.MATCH(thisNode, Token.NOUN);
    }

    // <ADJ_LIST> ::= <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
    private void ADJ_LIST(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.ADJECTIVE) {
            this.MATCH(thisNode, Token.ADJECTIVE);
            this.ADJ_TAIL(thisNode);
        } else {
            this.EMPTY(thisNode);
        }
    }

    // <ADJ_TAIL> ::= <COMMA> <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
    private void ADJ_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.ADJ_SEP) {
            this.MATCH(thisNode, Token.ADJ_SEP);
            this.MATCH(thisNode, Token.ADJECTIVE);
            this.ADJ_TAIL(thisNode);
        } else {
            this.EMPTY(thisNode);
        }
    }

    // <VERB_PHRASE> ::= <ADVERB> <VERB> | <VERB>
    private void VERB_PHRASE(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.ADVERB) {
            this.MATCH(thisNode, Token.ADVERB);
        }

        this.MATCH(thisNode, Token.VERB);
    }

    // <PREP_PHRASE> ::= <PREPOSITION> <NOUN_PHRASE> | <<EMPTY>>
    private void PREP_PHRASE(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.PREPOSITION) {
            this.MATCH(thisNode, Token.PREPOSITION);
            this.NOUN_PHRASE(thisNode);
        } else {
            this.EMPTY(thisNode);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a an EMPTY terminal node (result of an Epsilon Production) to the parse tree.
     * Mainly, this is just done for better visualizing the complete parse tree.
     *
     * @param parentNode The parent of the terminal node.
     */
    void EMPTY(final TreeNode parentNode) {
        codeGenerator.addEmptyToTree(parentNode);
    }

    /**
     * Match the current token with the expected token.
     * If they match, add the token to the parse tree, otherwise throw an exception.
     *
     * @param parentNode    The parent of the terminal node.
     * @param expectedToken The token to be matched.
     * @throws ParseException Thrown if the token does not match the expected token.
     */
    void MATCH(final TreeNode parentNode, final Token expectedToken) throws ParseException {
        final Token currentToken = lexer.currentToken();

        if (currentToken == expectedToken) {
            var currentLexeme = lexer.getCurrentLexeme();
            this.addTerminalToTree(parentNode, currentToken, currentLexeme);
            lexer.advanceToken();
        } else {
            this.raiseException(expectedToken, parentNode);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a terminal node to the parse tree.
     *
     * @param parentNode    The parent of the terminal node.
     * @param currentToken  The token to be added.
     * @param currentLexeme The lexeme of the token beign added.
     * @throws ParseException Throws a ParseException if the token cannot be added to the tree.
     */
    void addTerminalToTree(final TreeNode parentNode, final Token currentToken, final String currentLexeme) throws ParseException {
        var nodeLabel = "<%s>".formatted(currentToken);
        var terminalNode = codeGenerator.addNonTerminalToTree(parentNode, nodeLabel);

        codeGenerator.addTerminalToTree(terminalNode, currentLexeme);
    }

    /**
     * Raise a ParseException if the input cannot be parsed as defined by the grammar.
     *
     * @param expected   The expected token
     * @param parentNode The token's parent node
     */
    private void raiseException(Token expected, TreeNode parentNode) throws ParseException {
        final var template = "SYNTAX ERROR: '%s' was expected but '%s' was found.";
        final var errorMessage = template.formatted(expected.name(), lexer.getCurrentLexeme());
        codeGenerator.syntaxError(errorMessage, parentNode);
    }
}
