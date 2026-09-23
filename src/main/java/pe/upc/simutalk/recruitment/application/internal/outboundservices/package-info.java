/**
 * Outbound services of the recruitment context: anti-corruption layers towards
 * external systems or other bounded contexts.
 * <p>
 * Empty for now. Because no context may import classes from another one (only
 * {@code shared}), any integration placed here must go through contracts defined in
 * {@code shared} or through integration events, never through another context's
 * domain or application classes.
 */
package pe.upc.simutalk.recruitment.application.internal.outboundservices;
