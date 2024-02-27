grammar QueryGrammar;

query: 'SELECT' columns 'FROM' source ('WHERE' condition)? ';';

columns: 'REGIONS' (',' '[' projections ']')? ';';

projections: projection (',' projection)*;

projection: 'REGIONS' '.' property | 'REGIONS' '.' property ',' 'REGIONS' '.' property;

property: 'p' | 'HET';

source: '(' dataset | path ')';

dataset: 'dataset_name';

path: 'path';

condition: assignment | objective | order_by | optimization | 'GAPLESS' | 'HEURISTIC' '(' heuristic_type ')';

assignment: attribute '=' '(' value | '𝑝𝑀𝐴𝑋' ')';

attribute: 'p' | 'OBJECTIVE' '(' objective_type ') ON' attribute_name;

objective: 'OBJECTIVE' '(' objective_type ') ON' attribute_name;

objective_type: 'HETEROGENEOUS' | 'COMPACT';

order_by: 'ORDER BY' '(' order_by_type '|' order_by_type ')' ('(' order_direction '|' order_direction ')')? ';';

order_by_type: 'HET' | 'CARD';

order_direction: 'ASC' | 'DESC';

optimization: 'OPTIMIZATION' '(' optimization_type ')';

optimization_type: 'RANDOM' | 'CONNECTED';

value: comparison value | numeric_value | string_value;

comparison: '<' | '<=';

numeric_value: 'SUM' | 'MIN' | 'MAX' | 'COUNT' | 'AVG';

string_value: 'dataset_name' | 'path';

attribute_name: 'attribute_name';

heuristic_type: 'MSA' | 'TABU';
